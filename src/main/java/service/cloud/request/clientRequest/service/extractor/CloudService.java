package service.cloud.request.clientRequest.service.extractor;

import com.google.gson.Gson;
import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.Unirest;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import service.cloud.request.clientRequest.config.ProviderProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.request.RequestPost;
import service.cloud.request.clientRequest.dto.response.Data;
import service.cloud.request.clientRequest.extras.ISunatConnectorConfig;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.mongo.model.Log;
import service.cloud.request.clientRequest.mongo.model.LogDTO;
import service.cloud.request.clientRequest.mongo.model.DocumentPublication;
import service.cloud.request.clientRequest.mongo.service.ILogService;
import service.cloud.request.clientRequest.mongo.service.IDocumentPublicationService;
import service.cloud.request.clientRequest.service.emision.interfac.GuiaInterface;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceBaja;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceEmision;
import service.cloud.request.clientRequest.notification.service.NotificationManager;
import service.cloud.request.clientRequest.service.publicar.PublicacionManager;
import service.cloud.request.clientRequest.utils.Constants;
import service.cloud.request.clientRequest.utils.JsonUtils;
import service.cloud.request.clientRequest.utils.files.UtilsFile;


import java.io.IOException;
import java.util.*;

@Service
public class CloudService implements CloudInterface {

    Logger logger = LoggerFactory.getLogger(CloudService.class);


    @Autowired
    ProviderProperties providerProperties;

    @Autowired
    PublicacionManager publicacionManager;

    @Autowired
    NotificationManager notificationManager;

    @Autowired
    GuiaInterface iServiceEmisionGuia;

    @Autowired
    IServiceEmision iServiceEmision;

    @Autowired
    IServiceBaja iServiceBaja;

    @Autowired
    private ILogService logEntryService;

    @Autowired
    private IDocumentPublicationService publicarService;

    @Autowired
    @Qualifier("defaultMapper")
    private ModelMapper mapper;

    @Override
    public Mono<ResponseEntity<Object>> proccessDocument(String stringRequestOnpremise) {
        String datePattern = Constants.PATTERN_ARRAY_TRANSACTION;
        String updatedJson = stringRequestOnpremise.replaceAll(datePattern, "$1\"");

        return Mono.fromCallable(() -> {
                    try {
                        return JsonUtils.fromJson(updatedJson, TransacctionDTO[].class);
                    } catch (Exception e) {
                        throw new RuntimeException("Error parseando JSON de la transacción", e);
                    }
                })
                .flatMapMany(Flux::fromArray) // Convertir el array en flujo reactivo
                .flatMap(transaccion ->
                        {
                            try {
                                return processTransaction(transaccion, stringRequestOnpremise)
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .doOnError(error -> logger.error("Error procesando transacción: {}", error.getMessage()));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        },
                        100) // paralelismo
                .onErrorContinue((error, obj) ->
                        logger.warn("Continuando tras error: {} en transacción: {}", error.getMessage(), obj))
                .then() // Ignorar resultados, solo esperar a que termine
                .map(ignored -> ResponseEntity.ok().build()) // HTTP 200 OK
                .onErrorResume(error -> {
                    logger.error("Error general al procesar documentos: {}", error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }


    public void anexarDocumentos(RequestPost request) {

        try {
            String jsonBody = JsonUtils.toJson(request);

            HttpResponse<String> response = Unirest.post(request.getUrlOnpremise() + "anexar")
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .asString();

            logger.info("Se envió de manera correcta al servidor OnPremise los documentos.");
            logger.info("Estado de la respuesta del servidor OnPremise: " + response.getStatus());
        } catch (Exception e) {
            logger.error("Error de conexión con el servidor destino: " + e.getMessage());
            logger.error("No se pudo dejar los doucmentos en la ruta ruta del servitor: " + request.getUrlOnpremise());
        }

    }

    public String limpiarTexto(String texto) {
        if (texto == null) return null;
        return texto
                .replaceAll("[\\r\\n]", " ")   // Elimina \r y \n reales
                .replaceAll("\\s{2,}", " ")    // Colapsa espacios múltiples
                .trim();                       // Elimina espacios en extremos
    }

    private Mono<RequestPost> processTransaction(TransacctionDTO transaccion, String requestOnPremise) throws Exception {
        return enviarTransaccion(transaccion) // Paso 1: Enviar transacción
                .flatMap(tr -> {
                    // Paso 2: Generar datos de solicitud
                    String cleanString = limpiarTexto(transaccion.getFE_Comentario());
                    RequestPost request = generateDataRequestHana(transaccion, tr);

                    // Paso 3: Anexar documentos
                    anexarDocumentos(request);

                    // Paso 4: Limpieza de memoria (ayuda al GC)
                    liberarRecursosPesados(tr, request);

                    // Paso 5: Log de seguimiento
                    logTransaccionExitosa(request, tr);

                    // Paso 6: Guardado en Mongo / Publicación
                    handleLogsAndDocuments(tr, requestOnPremise);

                    return Mono.just(request);
                });
    }

    private void handleLogsAndDocuments(TransaccionRespuesta tr, String requestOnPremise) {
        if (tr.getLogDTO() != null) {
            try {
                saveRequestToFile(tr.getLogDTO(), requestOnPremise);
            } catch (IOException e) {
                logger.error("Error al guardar el archivo JSON en el path: " + tr.getLogDTO().getPathBase(), e);
            }
            logEntryService.saveLogEntryToMongoDB(convertToEntity(normalizeFilePaths(tr.getLogDTO()))).subscribe();

            if (tr.getLogDTO().getResponse().contains("acept")) {
                DocumentPublication publication = createDocumentPublication(tr.getLogDTO());
                publicarService.saveLogEntryToMongoDB(publication).subscribe();
            }
        }
    }

    private LogDTO normalizeFilePaths(LogDTO logDTO) {
        logDTO.setRequest(logDTO.getRequest().replace("\\", "/"));
        logDTO.setPathThirdPartyRequestXml(logDTO.getPathThirdPartyRequestXml().replace("\\", "/"));
        logDTO.setPathThirdPartyResponseXml(logDTO.getPathThirdPartyResponseXml().replace("\\", "/"));
        logDTO.setPathBase(logDTO.getPathBase().replace("\\", "/"));
        return logDTO;
    }


    private DocumentPublication createDocumentPublication(LogDTO logDTO) {
        DocumentPublication publication = new DocumentPublication();
        publication.setRuc(logDTO.getRuc());
        publication.setBusinessName(logDTO.getBusinessName());
        publication.setPathPdf(logDTO.getRequest().replace(".json", ".pdf"));
        publication.setPathXml(logDTO.getPathThirdPartyRequestXml());
        publication.setPathZip(logDTO.getPathThirdPartyResponseXml());
        publication.setObjectTypeAndDocEntry(logDTO.getObjectTypeAndDocEntry());
        publication.setSeriesAndCorrelative(logDTO.getSeriesAndCorrelative());
        return publication;
    }

    private void saveRequestToFile(LogDTO logDTO, String requestOnPremise) throws IOException {
        UtilsFile.saveJsonToFile(requestOnPremise, logDTO.getPathBase());
        logDTO.setRequest(logDTO.getPathBase());
    }

    private Log convertToEntity(LogDTO dto) {
        return mapper.map(dto, Log.class);
    }

    public Mono<TransaccionRespuesta> enviarTransaccion(TransacctionDTO transaction) throws Exception {
        if (transaction == null) {
            return Mono.just(new TransaccionRespuesta());
        }

        String tipoTransaccion = transaction.getFE_TipoTrans();
        String codigoDocumento = transaction.getDOC_Codigo();

        switch (tipoTransaccion.toUpperCase()) {
            case ISunatConnectorConfig.FE_TIPO_TRANS_EMISION:
                if (IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE.equalsIgnoreCase(codigoDocumento)
                        || IUBLConfig.DOC_SENDER_CARRIER_GUIDE_CODE.equalsIgnoreCase(codigoDocumento)) {
                    return iServiceEmisionGuia.transactionRemissionGuideDocumentRestReactive(
                            transaction, IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE);
                }
                return iServiceEmision.transactionDocument(transaction, codigoDocumento);

            case ISunatConnectorConfig.FE_TIPO_TRANS_BAJA:
                return iServiceBaja.transactionVoidedDocument(transaction, codigoDocumento);

            default:
                return Mono.just(new TransaccionRespuesta());
        }
    }

    public RequestPost generateDataRequestHana(TransacctionDTO tc, TransaccionRespuesta tr) {
        RequestPost request = new RequestPost();
        try {

            request.setRuc(tc.getDocIdentidad_Nro());
            request.setDocType(tc.getDOC_Codigo());
            request.setDocEntry(tc.getFE_DocEntry().toString());
            request.setDocObject(tc.getFE_ObjectType());
            request.setDocumentName(tr.getIdentificador());
            request.setTicketBaja(tr.getTicketRest());
            request.setRucClient(tc.getSN_DocIdentidad_Nro());

            request.setUrlOnpremise(providerProperties.getUrlOnpremise(tc.getDocIdentidad_Nro()));

            if (tr.getMensaje().contains("ha sido aceptad") || tr.getMensaje().contains("aprobado")) {
                tr.setPdf(tr.getPdfBorrador());
                tr.setEstado("V"); //Aprobado
                Map<String, Data.ResponseDocument> listMapDocuments = new HashMap<>();
                if (tr.getMensaje().contains("anulación") || tr.getMensaje().contains("resumen de comprobante número") || tr.getMensaje().contains("Baja") || tr.getMensaje().contains("El Resumen diario RC-") || tr.getMensaje().contains("El Resumen de Boletas número RC") || (tr.getMensaje().contains("El Resumen de Boletas numero"))) {
                    Data.ResponseDocument document4 = new Data.ResponseDocument("zip", tr.getZip());
                    listMapDocuments.put("cdr_baja", document4);
                    // Ensure pdf_borrador is always present when possible
                    byte[] borrador = tr.getPdfBorrador() != null ? tr.getPdfBorrador() : tr.getPdf();
                    if (borrador != null) {
                        Data.ResponseDocument draftDoc = new Data.ResponseDocument("pdf", borrador);
                        listMapDocuments.put("pdf_borrador", draftDoc);
                    }
                } else {
                    Data.ResponseDocument document1 = new Data.ResponseDocument("pdf", tr.getPdf());
                    Data.ResponseDocument document2 = new Data.ResponseDocument("xml", tr.getXml());
                    Data.ResponseDocument document3 = new Data.ResponseDocument("zip", tr.getZip());

                    listMapDocuments.put("pdf", document1);
                    listMapDocuments.put("xml", document2);
                    listMapDocuments.put("zip", document3);
                    if (tr.getPdfBorrador() != null) {
                        Data.ResponseDocument document4 = new Data.ResponseDocument("pdf", tr.getPdfBorrador());
                        listMapDocuments.put("pdf_borrador", document4);
                    }
                    // Ensure pdf_borrador exists even if tr.getPdfBorrador() is null
                    if (!listMapDocuments.containsKey("pdf_borrador")) {
                        byte[] borrador = tr.getPdf() != null ? tr.getPdf() : tr.getPdfBorrador();
                        if (borrador != null) {
                            Data.ResponseDocument draftDoc = new Data.ResponseDocument("pdf", borrador);
                            listMapDocuments.put("pdf_borrador", draftDoc);
                        }
                    }
                }

                request.setDigestValue(tr.getDigestValue());
                request.setBarcodeValue(tr.getBarcodeValue());

                Data.ResponseRequest responseRequest = new Data.ResponseRequest();
                responseRequest.setServiceResponse(tr.getMensaje());
                responseRequest.setListMapDocuments(listMapDocuments);
                request.setResponseRequest(responseRequest);
                request.setStatus(200);
                //publicacionManager.publicarDocumento(tc, tc.getFE_Id(), tr);
                notificationManager.enqueue(tc, tr);

            } else {
                logger.warn(tr.getMensaje());

                // Build consolidated listMapDocuments for error cases
                Map<String, Data.ResponseDocument> listMapDocuments = new HashMap<>();

                // Always try to include pdf_borrador with fallback to PDF if available
                byte[] borrador = tr.getPdfBorrador() != null ? tr.getPdfBorrador() : tr.getPdf();
                if (borrador != null) {
                    Data.ResponseDocument draftDoc = new Data.ResponseDocument("pdf", borrador);
                    listMapDocuments.put("pdf_borrador", draftDoc);
                }

                // Include ZIP if available
                if (tr.getZip() != null) {
                    Data.ResponseDocument zipDoc = new Data.ResponseDocument("zip", tr.getZip());
                    listMapDocuments.put("zip", zipDoc);
                }

                // Set response request if we have any documents
                if (!listMapDocuments.isEmpty()) {
                    Data.ResponseRequest responseRequest = new Data.ResponseRequest();
                    responseRequest.setServiceResponse(tr.getMensaje());
                    responseRequest.setListMapDocuments(listMapDocuments);
                    request.setResponseRequest(responseRequest);
                }

                Data.Error error = new Data.Error();
                Data.ErrorRequest errorRequest = new Data.ErrorRequest();
                errorRequest.setCode("500");
                errorRequest.setDescription(tr.getMensaje());
                error.setErrorRequest(errorRequest);
                request.setResponseError(error);
                request.setStatus(500);
            }
        } catch (Exception e) {
            logger.error("Error : " + e.getMessage());
        }
        return request;
    }

    private void liberarRecursosPesados(TransaccionRespuesta tr, RequestPost request) {
        tr.setPdf(null);
        tr.setXml(null);
        tr.setZip(null);
        tr.setPdfBorrador(null);
        request.setResponseRequest(null);

        // Opcional: solo si estás seguro que hay presión de memoria fuerte
        System.gc();
    }

    private void logTransaccionExitosa(RequestPost request, TransaccionRespuesta tr) {
        logger.info("================================ LOG DE TRANSACCIÓN ================================");
        logger.info("RUC: {}, DocObject: {}, DocEntry: {}", request.getRuc(), request.getDocObject(), request.getDocEntry());
        logger.info("Nombre del Documento: {}", request.getDocumentName());
        logger.info("Documentos anexados correctamente en SAP.");
        logger.info(tr.getMensaje());
        logger.info("===================================================================================");
    }

}
