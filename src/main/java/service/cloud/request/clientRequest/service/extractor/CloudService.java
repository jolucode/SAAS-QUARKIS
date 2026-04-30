package service.cloud.request.clientRequest.service.extractor;

import com.google.gson.Gson;
import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.Unirest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.cloud.request.clientRequest.config.ProviderProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.request.RequestPost;
import service.cloud.request.clientRequest.dto.response.Data;
import service.cloud.request.clientRequest.extras.ISunatConnectorConfig;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.mongo.model.DocumentPublication;
import service.cloud.request.clientRequest.mongo.model.Log;
import service.cloud.request.clientRequest.mongo.model.LogDTO;
import service.cloud.request.clientRequest.mongo.service.IDocumentPublicationService;
import service.cloud.request.clientRequest.mongo.service.ILogService;
import service.cloud.request.clientRequest.notification.service.NotificationManager;
import service.cloud.request.clientRequest.service.emision.interfac.GuiaInterface;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceBaja;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceEmision;
import service.cloud.request.clientRequest.service.publicar.PublicacionManager;
import service.cloud.request.clientRequest.utils.Constants;
import service.cloud.request.clientRequest.utils.JsonUtils;
import service.cloud.request.clientRequest.utils.files.UtilsFile;

import java.io.IOException;
import java.util.*;

@ApplicationScoped
public class CloudService implements CloudInterface {

    Logger logger = LoggerFactory.getLogger(CloudService.class);

    @Inject
    ProviderProperties providerProperties;

    @Inject
    PublicacionManager publicacionManager;

    @Inject
    NotificationManager notificationManager;

    @Inject
    GuiaInterface iServiceEmisionGuia;

    @Inject
    IServiceEmision iServiceEmision;

    @Inject
    IServiceBaja iServiceBaja;

    @Inject
    private ILogService logEntryService;

    @Inject
    private IDocumentPublicationService publicarService;

    @Inject
    @Named("defaultMapper")
    private ModelMapper mapper;

    @Override
    public Uni<Response> proccessDocument(String stringRequestOnpremise) {
        String datePattern = Constants.PATTERN_ARRAY_TRANSACTION;
        String updatedJson = stringRequestOnpremise.replaceAll(datePattern, "$1\"");

        return Uni.createFrom().item(Unchecked.supplier(() -> {
                    logger.debug("JSON recibido (primeros 500 chars): {}", updatedJson.length() > 500 ? updatedJson.substring(0, 500) : updatedJson);
                    return JsonUtils.fromJson(updatedJson, TransacctionDTO[].class);
                }))
                .onFailure().invoke(e -> logger.error("Fallo al parsear JSON. Causa: {}. JSON recibido: {}", e.getMessage(),
                        stringRequestOnpremise.length() > 300 ? stringRequestOnpremise.substring(0, 300) : stringRequestOnpremise))
                .onFailure().transform(e -> new RuntimeException("Error parseando JSON de la transacción", e))
                .chain(transactions -> Multi.createFrom().iterable(Arrays.asList(transactions))
                        .onItem().transformToUniAndMerge(transaccion -> {
                            try {
                                return processTransaction(transaccion, stringRequestOnpremise)
                                        .onFailure().invoke(error -> logger.warn("Continuando tras error: {} en transacción: {}", error.getMessage(), transaccion))
                                        .onFailure().recoverWithNull();
                            } catch (Exception e) {
                                logger.warn("Error procesando transacción: {}", e.getMessage());
                                return Uni.createFrom().nullItem();
                            }
                        })
                        .collect().asList()
                )
                .replaceWith(Response.ok().build())
                .onFailure().recoverWithItem(error -> {
                    logger.error("Error general al procesar documentos: {}", error.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                });
    }

    public void anexarDocumentos(RequestPost request) {
        try {
            String jsonBody = JsonUtils.toJson(request);
            HttpResponse<String> response = Unirest.post(request.getUrlOnpremise() + "anexar")
                    .header("Content-Type", "application/json")
                    .connectTimeout(5000)
                    .socketTimeout(8000)
                    .body(jsonBody)
                    .asString();
            logger.info("Se envió de manera correcta al servidor OnPremise los documentos.");
            logger.info("Estado de la respuesta del servidor OnPremise: " + response.getStatus());
        } catch (Exception e) {
            logger.error("Error de conexión con el servidor destino: " + e.getMessage());
            logger.error("No se pudo dejar los documentos en la ruta del servidor: " + request.getUrlOnpremise());
        }
    }

    public String limpiarTexto(String texto) {
        if (texto == null) return null;
        return texto.replaceAll("[\\r\\n]", " ").replaceAll("\\s{2,}", " ").trim();
    }

    private Uni<RequestPost> processTransaction(TransacctionDTO transaccion, String requestOnPremise) throws Exception {
        return enviarTransaccion(transaccion)
                .map(tr -> {
                    RequestPost request = generateDataRequestHana(transaccion, tr);
                    anexarDocumentos(request);
                    liberarRecursosPesados(tr, request);
                    logTransaccionExitosa(request, tr);
                    handleLogsAndDocuments(tr, requestOnPremise);
                    return request;
                });
    }

    private void handleLogsAndDocuments(TransaccionRespuesta tr, String requestOnPremise) {
        if (tr.getLogDTO() != null) {
            try {
                saveRequestToFile(tr.getLogDTO(), requestOnPremise);
            } catch (IOException e) {
                logger.error("Error al guardar el archivo JSON en el path: " + tr.getLogDTO().getPathBase(), e);
            }
            logEntryService.saveLogEntryToMongoDB(convertToEntity(normalizeFilePaths(tr.getLogDTO())))
                    .subscribe().with(v -> {}, e -> logger.error("Error guardando log MongoDB: {}", e.getMessage()));

            if (tr.getLogDTO().getResponse().contains("acept")) {
                DocumentPublication publication = createDocumentPublication(tr.getLogDTO());
                publicarService.saveLogEntryToMongoDB(publication)
                        .subscribe().with(v -> {}, e -> logger.error("Error guardando publicación MongoDB: {}", e.getMessage()));
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

    public Uni<TransaccionRespuesta> enviarTransaccion(TransacctionDTO transaction) throws Exception {
        if (transaction == null) {
            return Uni.createFrom().item(new TransaccionRespuesta());
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
                return Uni.createFrom().item(new TransaccionRespuesta());
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
                tr.setEstado("V");
                Map<String, Data.ResponseDocument> listMapDocuments = new HashMap<>();
                if (tr.getMensaje().contains("anulación") || tr.getMensaje().contains("resumen de comprobante número")
                        || tr.getMensaje().contains("Baja") || tr.getMensaje().contains("El Resumen diario RC-")
                        || tr.getMensaje().contains("El Resumen de Boletas número RC") || tr.getMensaje().contains("El Resumen de Boletas numero")) {
                    Data.ResponseDocument document4 = new Data.ResponseDocument("zip", tr.getZip());
                    listMapDocuments.put("cdr_baja", document4);
                    byte[] borrador = tr.getPdfBorrador() != null ? tr.getPdfBorrador() : tr.getPdf();
                    if (borrador != null) {
                        listMapDocuments.put("pdf_borrador", new Data.ResponseDocument("pdf", borrador));
                    }
                } else {
                    listMapDocuments.put("pdf", new Data.ResponseDocument("pdf", tr.getPdf()));
                    listMapDocuments.put("xml", new Data.ResponseDocument("xml", tr.getXml()));
                    listMapDocuments.put("zip", new Data.ResponseDocument("zip", tr.getZip()));
                    if (tr.getPdfBorrador() != null) {
                        listMapDocuments.put("pdf_borrador", new Data.ResponseDocument("pdf", tr.getPdfBorrador()));
                    }
                    if (!listMapDocuments.containsKey("pdf_borrador")) {
                        byte[] borrador = tr.getPdf() != null ? tr.getPdf() : tr.getPdfBorrador();
                        if (borrador != null) {
                            listMapDocuments.put("pdf_borrador", new Data.ResponseDocument("pdf", borrador));
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
                notificationManager.enqueue(tc, tr);
            } else {
                logger.warn(tr.getMensaje());
                Map<String, Data.ResponseDocument> listMapDocuments = new HashMap<>();
                byte[] borrador = tr.getPdfBorrador() != null ? tr.getPdfBorrador() : tr.getPdf();
                if (borrador != null) {
                    listMapDocuments.put("pdf_borrador", new Data.ResponseDocument("pdf", borrador));
                }
                if (tr.getZip() != null) {
                    listMapDocuments.put("zip", new Data.ResponseDocument("zip", tr.getZip()));
                }
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
