package service.cloud.request.clientRequest.service.emision;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.Unirest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.config.ApplicationProperties;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.extras.ISignerConfig;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.handler.FileHandler;
import service.cloud.request.clientRequest.handler.UBLDocumentHandler;
import service.cloud.request.clientRequest.handler.document.DocumentNameHandler;
import service.cloud.request.clientRequest.handler.document.SignerHandler;
import service.cloud.request.clientRequest.handler.refactorPdf.service.impl.BaseDocumentService;
import service.cloud.request.clientRequest.model.Client;
import service.cloud.request.clientRequest.model.model.ResponseDTO;
import service.cloud.request.clientRequest.model.model.ResponseDTOAuth;
import service.cloud.request.clientRequest.mongo.model.GuiaTicket;
import service.cloud.request.clientRequest.mongo.model.LogDTO;
import service.cloud.request.clientRequest.mongo.repo.IGuiaTicketRepo;
import service.cloud.request.clientRequest.service.core.DocumentFormatInterface;
import service.cloud.request.clientRequest.service.core.ProcessorCoreInterface;
import service.cloud.request.clientRequest.service.emision.interfac.GuiaInterface;
import service.cloud.request.clientRequest.utils.JsonUtils;
import service.cloud.request.clientRequest.utils.SunatResponseUtils;
import service.cloud.request.clientRequest.utils.ValidationHandler;
import service.cloud.request.clientRequest.utils.exception.DateUtils;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.utils.files.CertificateUtils;
import service.cloud.request.clientRequest.utils.files.DocumentConverterUtils;
import service.cloud.request.clientRequest.utils.files.DocumentNameUtils;
import service.cloud.request.clientRequest.utils.files.UtilsFile;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.despatchadvice_2.DespatchAdviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import java.io.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * -----------------------------------------------------------------------------
 * <p>
 * Proyecto          : facturación SAAS
 * <p>
 * conforme a las especificaciones de SUNAT.
 * <p>
 * Autor             : Jose Luis Becerra
 * Rol               : Software Developer Senior
 * Fecha de creación : 09/07/2025
 * -----------------------------------------------------------------------------
 */

@Service
public class GuiaServiceImpl extends BaseDocumentService implements GuiaInterface {

    private static final Logger logger = LoggerFactory.getLogger(GuiaServiceImpl.class);

    //Estados
    private static final String ESTADO_NUEVO = "N";
    private static final String ESTADO_CONTINGENCIA = "C";
    private static final String VALOR_CONTINGENCIA = "Si";
    private static final String EXT_XML = "xml";
    private static final String EXT_ZIP = "zip";
    private static final String EMPTY = "";
    private static final GuiaTicket EMPTY_TICKET = new GuiaTicket();
    @Autowired
    private ClientProperties clientProperties;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private ProcessorCoreInterface processorCoreInterface;

    @Autowired
    private DocumentFormatInterface documentFormatInterface;

    @Autowired
    private IGuiaTicketRepo guiaTicketRepo;

    private String docUUID;

    @Override
    public Mono<TransaccionRespuesta> transactionRemissionGuideDocumentRestReactive(TransacctionDTO transaction, String doctype) throws Exception {
        LogDTO log = new LogDTO();
        log.setRequestDate(DateUtils.formatDateToString(new Date()));
        log.setRuc(transaction.getDocIdentidad_Nro());
        log.setBusinessName(transaction.getSN_RazonSocial());

        TransaccionRespuesta response = new TransaccionRespuesta();
        String signerName = ISignerConfig.SIGNER_PREFIX + transaction.getDocIdentidad_Nro();

        boolean isContingencia = transaction.getTransactionContractDocRefListDTOS().stream()
                .anyMatch(map -> VALOR_CONTINGENCIA.equalsIgnoreCase(map.get("cu31")));

        ValidationHandler validationHandler = ValidationHandler.newInstance(this.docUUID);
        validationHandler.checkBasicInformation(
                transaction.getDOC_Id(),
                transaction.getDocIdentidad_Nro(),
                transaction.getDOC_FechaEmision(),
                transaction.getSN_EMail(),
                transaction.getEMail(),
                isContingencia);

        Client client = clientProperties.listaClientesOf(transaction.getDocIdentidad_Nro());
        String certificatePath = applicationProperties.getRutaBaseDocConfig() + transaction.getDocIdentidad_Nro() + File.separator + client.getCertificadoName();
        byte[] certificado = CertificateUtils.getCertificateInBytes(certificatePath);
        CertificateUtils.checkDigitalCertificateV2(certificado, client.getCertificadoPassword(), applicationProperties.getSupplierCertificate(), applicationProperties.getKeystoreCertificateType());

        UBLDocumentHandler ublHandler = UBLDocumentHandler.newInstance(this.docUUID);
        DespatchAdviceType despatchAdviceType = ublHandler.generateDespatchAdviceType(transaction, signerName);
        String documentName = DocumentNameHandler.getInstance().getRemissionGuideName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id());

        FileHandler fileHandler = FileHandler.newInstance(this.docUUID);
        Calendar fechaEmision = Calendar.getInstance();
        fechaEmision.setTime(transaction.getDOC_FechaEmision());
        String attachmentPath = applicationProperties.getRutaBaseDocAnexos() + transaction.getDocIdentidad_Nro() +
                File.separator + "anexo" + File.separator + fechaEmision.get(Calendar.YEAR) + File.separator + (fechaEmision.get(Calendar.MONTH) + 1) + File.separator + fechaEmision.get(Calendar.DAY_OF_MONTH) +
                File.separator + transaction.getSN_DocIdentidad_Nro() + File.separator + doctype;
        fileHandler.setBaseDirectory(attachmentPath);

        String documentPath = fileHandler.storeDocumentInDisk(despatchAdviceType, documentName);
        SignerHandler signerHandler = SignerHandler.newInstance();
        signerHandler.setConfiguration(certificado, client.getCertificadoPassword(), applicationProperties.getKeystoreCertificateType(), applicationProperties.getSupplierCertificate(), signerName);
        File signedDocument = signerHandler.signDocument(documentPath, docUUID);

        Object ublDocument = fileHandler.getSignedDocument(signedDocument, transaction.getDOC_Codigo());
        UBLDocumentWRP wrp = new UBLDocumentWRP();
        wrp.setTransaccion(transaction);
        wrp.setAdviceType((DespatchAdviceType) ublDocument);

        byte[] xmlDoc = DocumentConverterUtils.convertDocumentToBytes(despatchAdviceType);
        byte[] signedXml = signerHandler.signDocumentv2(xmlDoc, docUUID);
        UtilsFile.storeDocumentInDisk(signedXml, documentName, EXT_XML, attachmentPath);

        DataHandler zipDocument = UtilsFile.compressUBLDocument(signedXml, documentName + ".xml");
        if (zipDocument == null) {
            return Mono.error(new NullPointerException(IVenturaError.ERROR_457.getMessage()));
        }

        ConfigData config = createConfigData(client);
        log.setThirdPartyServiceInvocationDate(DateUtils.formatDateToString(new Date()));

        byte[] pdfBorradorBytes = null;
        try {
            String attachmentPathPDF = UtilsFile.getAttachmentPath(transaction, "09", applicationProperties.getRutaBaseDocAnexos());
            String documentNamePDF = DocumentNameUtils.getDocumentName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id(), "15");
            pdfBorradorBytes = documentFormatInterface.createPDFDocument(wrp, transaction, config);
            UtilsFile.storeDocumentInDisk(pdfBorradorBytes, documentNamePDF + "_borrador", "pdf", attachmentPathPDF);
            logger.info("PDF borrador generado exitosamente: " + attachmentPathPDF);
        }catch (Exception e) {
            logger.error("Error al generar PDF borrador: " + e.getMessage());
        }

        Mono<TransaccionRespuesta> resultado;
        if (ESTADO_NUEVO.equalsIgnoreCase(transaction.getFE_Estado())) {
            resultado = processEstadoNReactive(transaction, config, fileHandler, signedDocument, signedXml, documentName, wrp);
        } else if (ESTADO_CONTINGENCIA.equalsIgnoreCase(transaction.getFE_Estado())) {
            resultado = processEstadoCReactive(transaction, config, fileHandler, signedDocument, documentName, wrp);
        } else {
            resultado = Mono.error(new IllegalStateException("Estado no reconocido: " + transaction.getFE_Estado()));
        }

        String digestValue = generateDigestValue(wrp.getAdviceType().getUBLExtensions());
        String barcodeValue = generateGuiaBarcodeInfoV2(wrp.getAdviceType().getID().getValue(), IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE,
                transaction.getDOC_FechaVencimiento(), BigDecimal.ZERO, BigDecimal.ZERO,
                wrp.getAdviceType().getDespatchSupplierParty(), wrp.getAdviceType().getDeliveryCustomerParty(),
                wrp.getAdviceType().getUBLExtensions());

        byte[] finalPdfBorradorBytes = pdfBorradorBytes;

        return resultado.map(trxResp -> {
            trxResp.setDigestValue(digestValue);
            trxResp.setBarcodeValue(barcodeValue);
            trxResp.setIdentificador(documentName);

            // ✅ Verificas si el PDF es borrador y lo seteas explícitamente
            if (config.getPdfBorrador() != null && config.getPdfBorrador().equals("true")) {
                //trxResp.setPdfBorrador(trxResp.getPdf()); // (esto puede parecer redundante, pero garantiza que se mantenga)
                trxResp.setPdfBorrador(finalPdfBorradorBytes); // (esto puede parecer redundante, pero garantiza que se mantenga)
                logger.info("PDF borrador detectado y mantenido en la respuesta");
            }

            log.setThirdPartyServiceResponseDate(DateUtils.formatDateToString(new Date()));
            log.setPathThirdPartyRequestXml(attachmentPath + "\\" + documentName + ".xml");
            log.setPathThirdPartyResponseXml(attachmentPath + "\\" + documentName + ".zip");
            log.setObjectTypeAndDocEntry(transaction.getFE_ObjectType() + " - " + transaction.getFE_DocEntry());
            log.setSeriesAndCorrelative(documentName);
            log.setResponse(JsonUtils.toJson(trxResp.getSunat()).equals("null") ? trxResp.getMensaje() : JsonUtils.toJson(trxResp.getSunat()));
            log.setResponseDate(DateUtils.formatDateToString(new Date()));
            log.setPathBase(attachmentPath + "\\" + documentName + ".json");
            trxResp.setLogDTO(log);

            // *** GUARDAR EL ZIP EN DISCO SI EXISTE ***
            if (trxResp.getZip() != null) {
                try {
                    // Define aquí la ruta donde quieres que se guarde el ZIP
                    UtilsFile.storeDocumentInDisk(trxResp.getZip(), documentName, "zip", attachmentPath);
                    logger.info("ZIP del CDR guardado en: " + attachmentPath + "\\" + documentName + ".zip");
                } catch (IOException e) {
                    logger.error("Error al guardar el ZIP del CDR: " + e.getMessage());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return trxResp;
        });
    }

    private ConfigData createConfigData(Client client) {
        return ConfigData.builder()
                .usuarioSol(client.getUsuarioSol())
                .claveSol(client.getClaveSol())
                .integracionWs(client.getIntegracionWs())
                .ambiente(applicationProperties.getAmbiente())
                .pdfBorrador(client.getPdfBorrador())
                .impresionPDF(client.getImpresion())
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .userNameSunatSunat(client.getUserNameSunatGuias())
                .passwordSunatSunat(client.getPasswordSunatGuias())
                .rutaBaseDoc(applicationProperties.getRutaBaseDocAnexos())
                .rutaBaseConfig(applicationProperties.getRutaBaseDocConfig())
                .build();
    }

    public Mono<TransaccionRespuesta> processEstadoNReactive(TransacctionDTO tx,
                                                             ConfigData config,
                                                             FileHandler fileHandler,
                                                             File signedDoc,
                                                             byte[] docBytes,
                                                             String docName,
                                                             UBLDocumentWRP wrp) {

        return guiaTicketRepo.findGuiaTicketByRucEmisorAndFeId(tx.getDocIdentidad_Nro(), tx.getFE_Id())
                .defaultIfEmpty(EMPTY_TICKET)
                .flatMap(existingTicket -> {

                    if (existingTicket != EMPTY_TICKET) {
                        logger.info("Ticket previo encontrado para RUC {} y FE_Id {}: ticket={}, estado={}",
                                tx.getDocIdentidad_Nro(), tx.getFE_Id(),
                                existingTicket.getTicketSunat(), existingTicket.getEstadoTicket());
                    } else {
                        logger.info("⚠️ No se encontró ticket previo para RUC {} y FE_Id {}. Se procederá a declarar uno nuevo.",
                                tx.getDocIdentidad_Nro(), tx.getFE_Id());
                    }

                    if (existingTicket != EMPTY_TICKET) {

                        logger.info("Ticket guía: {} (ticket: {}) | RUC-Tipo-Serie-Correlativo: {}-{}-{}-{}",
                                existingTicket.getEstadoTicket(), existingTicket.getTicketSunat(),
                                tx.getDocIdentidad_Nro(), tx.getDOC_Codigo(), tx.getDOC_Serie(), tx.getDOC_Numero());

                        return consultarYProcesar(existingTicket.getTicketSunat(), tx, config, wrp, fileHandler, signedDoc, docName);
                    }

                    logger.info("Se declarará un nuevo ticket para el documento {} con RUC {}", docName, tx.getDocIdentidad_Nro());

                    return Mono.fromCallable(() -> getJwtSunat(config))
                            .flatMap(token -> Mono.fromCallable(() -> declareSunat(docName, docBytes, token.getAccess_token())))
                            .doOnNext(resp -> {
                                logger.info("✅ Nuevo ticket creado en SUNAT: {} | RUC-Tipo-Serie-Correlativo: {}-{}-{}-{}",
                                        resp.getNumTicket(),
                                        tx.getDocIdentidad_Nro(), tx.getDOC_Codigo(), tx.getDOC_Serie(), tx.getDOC_Numero());
                                saveTicketRest(tx, resp);
                            })
                            .flatMap(resp -> consultarTicketConReintentos(resp.getNumTicket(), config))
                            .flatMap(respFinal -> manejarRespuestaSunatReactive(respFinal, wrp, fileHandler, docName, tx, config, signedDoc));
                })
                .timeout(Duration.ofSeconds(20))
                .onErrorResume(ex -> {
                    logger.error("Error controlado procesando transacción FE_Id={} doc={}-{}-{}: {}",
                            tx.getFE_Id(), tx.getDOC_Codigo(), tx.getDOC_Serie(), tx.getDOC_Numero(), obtenerMensajeControlado(ex));

                    TransaccionRespuesta respuesta = new TransaccionRespuesta();
                    //respuesta.setEstado(false);
                    respuesta.setMensaje(obtenerMensajeControlado(ex));
                    //respuesta.setMensaje("Error consultando SUNAT: " + ex.getMessage());

                    return Mono.just(respuesta);
                });
    }

    private String obtenerMensajeControlado(Throwable ex) {
        Throwable real = Exceptions.unwrap(ex);

        if (real instanceof TimeoutException) {
            return "SUNAT no respondió dentro del tiempo esperado";
        }

        if (real instanceof AsyncRequestTimeoutException) {
            return "La operación excedió el tiempo máximo de espera del servidor";
        }

        String msg = real.getMessage();

        if (msg != null && msg.contains("504 Gateway Time-out")) {
            return "SUNAT no disponible temporalmente (504 Gateway Time-out)";
        }

        if (msg != null && msg.contains("503 Service Temporarily Unavailable")) {
            return "SUNAT no disponible temporalmente (503 Service Temporarily Unavailable)";
        }

        if (msg != null && msg.contains("500 Internal Server Error")) {
            return "SUNAT presentó un error interno (500 Internal Server Error)";
        }

        if (msg != null && msg.contains("Unexpected character ('<'")) {
            return "SUNAT o el gateway devolvió HTML en lugar de JSON";
        }

        return "Error consultando SUNAT: " + (msg != null ? msg : real.getClass().getSimpleName());
    }

    public Mono<TransaccionRespuesta> manejarRespuestaSunatReactive(ResponseDTO responseDTO,
                                                                    UBLDocumentWRP wrp,
                                                                    FileHandler fileHandler,
                                                                    String docName,
                                                                    TransacctionDTO tx,
                                                                    ConfigData config,
                                                                    File signedDoc) {
        return Mono.fromCallable(() -> {
            TransaccionRespuesta respuesta = generateResponseRest(wrp, responseDTO);

            if ("0".equals(responseDTO.getCodRespuesta())) {
                // Aprobado → decodificar CDR
                String rptBase64 = responseDTO.getArcCdr();
                byte[] zipBytes = Base64.decodeBase64(rptBase64);
                respuesta.setZip(zipBytes);
                respuesta.setTicketRest(responseDTO.getNumTicket());

                // Actualiza URL PDF (opcional)
                String urlPdf = SunatResponseUtils.proccessResponseUrlPdfGuia(zipBytes);
                config.setUrlGuias(urlPdf);
            }

            // Generar PDF (no crítico — si falla se continúa sin PDF)
            try {
                byte[] pdf = processorCoreInterface.processCDRResponseContigencia(
                        null, fileHandler, docName, tx.getDOC_Codigo(), wrp, tx, config
                );
                respuesta.setPdf(pdf);
            } catch (Exception pdfEx) {
                logger.error("Error generando PDF para guía {} — se continúa sin PDF: {}", docName, pdfEx.getMessage());
            }

            // Adjuntar XML firmado
            byte[] xmlBytes = fileHandler.convertFileToBytes(signedDoc);
            respuesta.setXml(xmlBytes);

            return respuesta;
        });
    }


    public Mono<TransaccionRespuesta> consultarYProcesar(String ticket,
                                                         TransacctionDTO tx,
                                                         ConfigData config,
                                                         UBLDocumentWRP wrp,
                                                         FileHandler fileHandler,
                                                         File signedDoc,
                                                         String docName) {

        return Mono.fromCallable(() -> consultarTicketEnSunat(ticket, config))
                .flatMap(responseDTO -> {
                    updateTicketStatusFromConsulta(tx, responseDTO);

                    return manejarRespuestaSunatReactive(
                            responseDTO, wrp, fileHandler, docName, tx, config, signedDoc
                    );
                });
    }


    public Mono<ResponseDTO> consultarTicketConReintentos(String ticket, ConfigData config) {
        return Mono.defer(() -> Mono.fromCallable(() -> getJwtSunat(config)))
                .flatMap(jwt -> Mono.fromCallable(() -> consult(ticket, jwt.getAccess_token())))
                .delayElement(Duration.ofSeconds(5)) // espera inicial
                .expand(resp -> {
                    if ("98".equals(resp.getCodRespuesta())) {
                        return Mono.delay(Duration.ofSeconds(5))
                                .then(Mono.fromCallable(() -> {
                                    ResponseDTO jwtNew = getJwtSunat(config);
                                    return consult(ticket, jwtNew.getAccess_token());
                                }));
                    }
                    return Mono.empty();
                })
                .take(5)
                .last()
                .onErrorResume(e -> Mono.error(new RuntimeException("Error consultando ticket SUNAT", e)));
    }


    public void updateTicketStatusFromConsulta(TransacctionDTO transaccion, ResponseDTO responseDTO) {
        guiaTicketRepo.findGuiaTicketByRucEmisorAndFeId(
                transaccion.getDocIdentidad_Nro(),
                transaccion.getFE_Id()
        ).flatMap(existing -> {
            if (existing == null) {
                logger.warn("No se encontró ticket para actualizar luego de consulta SUNAT.");
                return Mono.empty();
            }

            String cod = responseDTO.getCodRespuesta();
            if ("99".equals(cod)) {
                logger.warn("Respuesta 99 en consulta SUNAT. Eliminando ticket...");
                return guiaTicketRepo.delete(existing)
                        .doOnSuccess(v -> logger.info("Ticket eliminado tras codRespuesta 99"));
            } else {
                if ("98".equals(cod))
                    existing.setEstadoTicket("PROCESO");
                else if ("0".equals(cod))
                    existing.setEstadoTicket("APROBADO");
                existing.setCreadoEn(DateUtils.formatDateToString(new Date()));
                return guiaTicketRepo.save(existing)
                        .doOnSuccess(v -> logger.info("Ticket actualizado a estado: " + existing.getEstadoTicket()));
            }
        }).subscribe();
    }


    public Mono<TransaccionRespuesta> processEstadoCReactive(TransacctionDTO tx,
                                                             ConfigData config,
                                                             FileHandler fileHandler,
                                                             File signedDoc,
                                                             String docName,
                                                             UBLDocumentWRP wrp) {

        return guiaTicketRepo.findGuiaTicketByRucEmisorAndFeId(tx.getDocIdentidad_Nro(), tx.getFE_Id())
                .defaultIfEmpty(new GuiaTicket()) // o usa un EMPTY_TICKET
                .flatMap(ticket -> {
                    if (ticket.getTicketSunat() == null || ticket.getTicketSunat().isEmpty()) {
                        logger.warn("No se encontró ticket previo en la BD para RUC: {} y FE_Id: {}", tx.getDocIdentidad_Nro(), tx.getFE_Id());
                        return Mono.just(new TransaccionRespuesta());
                    }

                    ResponseDTO responseDTO = null;
                    try {
                        responseDTO = consultarTicketEnSunat(ticket.getTicketSunat(), config);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    updateTicketStatusFromConsulta(tx, responseDTO); // si esto no tiene efectos colaterales está bien

                    TransaccionRespuesta response = manejarRespuestaSunat(responseDTO, wrp, fileHandler, docName, tx, config);
                    if (response != null) {
                        byte[] xmlBytes = fileHandler.convertFileToBytes(signedDoc);
                        response.setXml(xmlBytes);
                    }

                    logger.info("Ticket guía: {} | RUC-Tipo-Serie-Correlativo: {}-{}-{}-{}",
                            ticket.getEstadoTicket(),
                            tx.getDocIdentidad_Nro(), tx.getDOC_Codigo(), tx.getDOC_Serie(), tx.getDOC_Numero());

                    return Mono.just(response);
                });
    }


    public ResponseDTO consult(String numTicket, String token) throws JsonProcessingException {
        HttpResponse<String> response = Unirest.get("https://api-cpe.sunat.gob.pe/v1/contribuyente/gem/comprobantes/envios/" + numTicket)
                .header("Authorization", "Bearer " + token)
                .asString();
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseDTO responseDTO = objectMapper.readValue(response.body(), new TypeReference<ResponseDTO>() {
        });
        responseDTO.setStatusCode(response.getStatus());
        responseDTO.setNumTicket(numTicket);
        return responseDTO;
    }

    public ResponseDTO declareSunat(String documentName, byte[] fileContent, String token) throws IOException {

        // 1. Comprimir los bytes en memoria para generar un .zip
        //    Suponiendo que 'documentName + ".xml"' es el nombre interno del archivo dentro del ZIP.
        byte[] zipBytes = compressToZip(fileContent, documentName + ".xml");


        String fileTo64 = zipDocumentTo64(zipBytes);
        String fileTo256Sha = zipDocumentTo265Sha(zipBytes);
        String nombreArchivo = documentName + ".zip";

        HttpResponse<String> response = Unirest.post("https://api-cpe.sunat.gob.pe/v1/contribuyente/gem/comprobantes/" + documentName)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body("{\r\n    \"archivo\" : {\r\n        \"nomArchivo\" : \"" + nombreArchivo + "\",\r\n        \"arcGreZip\" : \"" + fileTo64 + "\",\r\n        \"hashZip\" : \"" + fileTo256Sha + "\"\r\n    }\r\n}")
                .asString();

        ObjectMapper objectMapper = new ObjectMapper();
        ResponseDTO responseDTO = objectMapper.readValue(response.body(), new TypeReference<ResponseDTO>() {
        });
        responseDTO.setStatusCode(response.getStatus());
        return responseDTO;
    }

    private byte[] compressToZip(byte[] fileContent, String internalFileName) throws IOException {
        if (fileContent == null || fileContent.length == 0) {
            return new byte[0];
        }

        // ByteArrayOutputStream para almacenar el ZIP en memoria
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // Crear una entrada en el ZIP con el nombre interno
            ZipEntry entry = new ZipEntry(internalFileName);
            zos.putNextEntry(entry);

            // Escribir los bytes en la entrada
            zos.write(fileContent, 0, fileContent.length);
            zos.closeEntry();
            zos.finish();

            // Retornar todos los bytes del ZIP
            return baos.toByteArray();
        }
    }

    public String zipDocumentTo265Sha(byte[] documentContent) {
        if (documentContent == null || documentContent.length == 0) {
            return "";
        }
        // DigestUtils.sha256Hex procesa los bytes y retorna el hash en formato hexadecimal
        return DigestUtils.sha256Hex(documentContent);
    }

    public String zipDocumentTo64(byte[] fileContent) {
        if (fileContent == null || fileContent.length == 0) {
            return null;
        }
        // Codifica los bytes en Base64 y los convierte a String
        return new String(Base64.encodeBase64(fileContent));
    }

    public ResponseDTO getJwtSunat(ConfigData configuracion) throws JsonProcessingException {

        HttpResponse<String> response = Unirest.post("https://api-seguridad.sunat.gob.pe/v1/clientessol/" + configuracion.getClientId() + "/oauth2/token/")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", "TS019e7fc2=014dc399cbcad00473c65166bab99ef2e22263ae15b2a9e259ff0e5d972578fa54549fe8acccb61b76d241060b054cc73beff45ea3")
                .field("grant_type", "password")
                .field("scope", "https://api-cpe.sunat.gob.pe/")
                .field("client_id", configuracion.getClientId())
                .field("client_secret", configuracion.getClientSecret())
                .field("username", configuracion.getUserNameSunatSunat())
                .field("password", configuracion.getPasswordSunatSunat())
                .field("", "")
                .asString();

        ObjectMapper objectMapper = new ObjectMapper();
        ResponseDTO responseDTO = new ResponseDTO();
        ResponseDTOAuth responseDTO400;

        if (response.getStatus() == 400) {
            responseDTO400 = objectMapper.readValue(response.body(), new TypeReference<ResponseDTOAuth>() {
            });
            responseDTO.setResponseDTO400(responseDTO400);
        } else {
            responseDTO = objectMapper.readValue(response.body(), new TypeReference<ResponseDTO>() {
            });
        }
        responseDTO.setStatusCode(response.getStatus());
        return responseDTO;
    }

    public TransaccionRespuesta generateResponseRest(UBLDocumentWRP documentWRP, ResponseDTO responseDTO) {
        TransaccionRespuesta.Sunat sunatResponse = new TransaccionRespuesta.Sunat();
        sunatResponse.setId(documentWRP.getTransaccion().getDOC_Serie() + "-" + documentWRP.getTransaccion().getDOC_Numero());

        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
        if (responseDTO.getStatusCode() == 400 || responseDTO.getStatusCode() == 401) {
            // Error de token o credenciales
            if (responseDTO.getResponseDTO400() != null) {
                transactionResponse.setMensaje(responseDTO.getResponseDTO400().getError() + " - " + responseDTO.getResponseDTO400().getError_description());
            } else {
                transactionResponse.setMensaje("Error no especificado");
            }
        } else if (responseDTO.getStatusCode() == 404) {
            transactionResponse.setMensaje("El número de ticket para consultar no existe");
            sunatResponse.setAceptado(false);
            transactionResponse.setSunat(sunatResponse);
        } else {
            // Manejo de códigos de respuesta
            if ("0".equals(responseDTO.getCodRespuesta())) {
                // Aprobado
                String rptBase64 = responseDTO.getArcCdr();
                byte[] bytesZip = Base64.decodeBase64(rptBase64);
                transactionResponse.setMensaje(responseDTO.getCodRespuesta() + " - " + "Documento aprobado");
                transactionResponse.setZip(bytesZip);
                sunatResponse.setAceptado(true);
                transactionResponse.setSunat(sunatResponse);

            } else if ("98".equals(responseDTO.getCodRespuesta())) {
                // En proceso
                transactionResponse.setMensaje(responseDTO.getCodRespuesta() + " - Documento en proceso, volver a consultar.");
                sunatResponse.setAceptado(false);
                transactionResponse.setSunat(sunatResponse);

            } else if ("99".equals(responseDTO.getCodRespuesta())) {
                // Rechazado
                if ("1".equals(responseDTO.getIndCdrGenerado())) {
                    String rptBase64 = responseDTO.getArcCdr();
                    byte[] bytesZip = Base64.decodeBase64(rptBase64);
                    transactionResponse.setZip(bytesZip);
                }
                transactionResponse.setMensaje(responseDTO.getError().getNumError() + " - " + responseDTO.getError().getDesError());
                sunatResponse.setAceptado(false);
                transactionResponse.setSunat(sunatResponse);
            }
        }
        return transactionResponse;
    }

    public void saveTicketRest(TransacctionDTO transaccion, ResponseDTO responseDTO) {
        guiaTicketRepo.findGuiaTicketByRucEmisorAndFeId(
                transaccion.getDocIdentidad_Nro(),
                transaccion.getFE_Id()
        ).hasElement().subscribe(exists -> {
            if (!exists) {
                GuiaTicket guiaTicket = new GuiaTicket();
                guiaTicket.setRucEmisor(transaccion.getDocIdentidad_Nro());
                guiaTicket.setFeId(transaccion.getFE_Id());
                guiaTicket.setTicketSunat(responseDTO.getNumTicket());
                guiaTicket.setCreadoEn(DateUtils.formatDateToString(new Date()));

                guiaTicketRepo.save(guiaTicket).subscribe();
            } else {
                logger.warn("Ya existe ticket para esta guía, no se vuelve a guardar.");
            }
        });
    }

    private ResponseDTO consultarTicketEnSunat(String ticket, ConfigData configuracion) throws JsonProcessingException {
        ResponseDTO responseDTOJWT = getJwtSunat(configuracion);
        ResponseDTO responseDTO = consult(ticket, responseDTOJWT.getAccess_token());
        if (responseDTO.getStatusCode() == 401) {
            // Renovar token y volver a intentar
            responseDTOJWT = getJwtSunat(configuracion);
            responseDTO = consult(ticket, responseDTOJWT.getAccess_token());
        }
        return responseDTO;
    }

    private TransaccionRespuesta manejarRespuestaSunat(ResponseDTO responseDTO,
                                                       UBLDocumentWRP documentWRP,
                                                       FileHandler fileHandler,
                                                       String documentName,
                                                       TransacctionDTO transaction,
                                                       ConfigData configuracion) {
        // Errores de credenciales o ticket
        if (responseDTO.getStatusCode() == 400 || responseDTO.getStatusCode() == 404) {
            return generateResponseRest(documentWRP, responseDTO);
        }

        TransaccionRespuesta transactionResponse = null;

        if (responseDTO.getCodRespuesta() != null) {
            switch (responseDTO.getCodRespuesta()) {
                case "0":
                    // Documento aprobado
                    transactionResponse = generateResponseRest(documentWRP, responseDTO);

                    // Procesar ZIP y URL de guía
                    String rptBase64 = responseDTO.getArcCdr();
                    byte[] bytesZip = Base64.decodeBase64(rptBase64);
                    String sunatResponseUrlPdfGuia = SunatResponseUtils.proccessResponseUrlPdfGuia(bytesZip);
                    configuracion.setUrlGuias(sunatResponseUrlPdfGuia);

                    transactionResponse.setTicketRest(responseDTO.getNumTicket());
                    break;

                case "98":
                case "99":
                    // Documento en proceso o rechazado
                    transactionResponse = generateResponseRest(documentWRP, responseDTO);
                    break;

                default:
                    transactionResponse = generateResponseRest(documentWRP, responseDTO);
                    break;
            }

            // Generar PDF para todos los casos válidos
            if (transactionResponse != null) {
                byte[] pdf = null;


                try {
                    pdf = processorCoreInterface.processCDRResponseContigencia(null,
                            fileHandler,
                            documentName,
                            transaction.getDOC_Codigo(),
                            documentWRP,
                            transaction,
                            configuracion);
                } catch (PDFReportException e) {
                    transactionResponse.setErrorPdf(e.getMessage());
                    throw new RuntimeException(e);
                }
                transactionResponse.setPdf(pdf);
            }

            return transactionResponse;
        }

        return null;
    }

}
