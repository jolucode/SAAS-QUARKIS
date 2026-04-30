package service.cloud.request.clientRequest.service.emision;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.Unirest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import javax.activation.DataHandler;
import java.io.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@ApplicationScoped
public class GuiaServiceImpl extends BaseDocumentService implements GuiaInterface {

    private static final Logger logger = LoggerFactory.getLogger(GuiaServiceImpl.class);

    private static final String ESTADO_NUEVO = "N";
    private static final String ESTADO_CONTINGENCIA = "C";
    private static final String VALOR_CONTINGENCIA = "Si";
    private static final String EXT_XML = "xml";
    private static final GuiaTicket EMPTY_TICKET = new GuiaTicket();

    @Inject private ClientProperties clientProperties;
    @Inject private ApplicationProperties applicationProperties;
    @Inject private ProcessorCoreInterface processorCoreInterface;
    @Inject private DocumentFormatInterface documentFormatInterface;
    @Inject private IGuiaTicketRepo guiaTicketRepo;

    private String docUUID;

    @Override
    public Uni<TransaccionRespuesta> transactionRemissionGuideDocumentRestReactive(TransacctionDTO transaction, String doctype) throws Exception {
        LogDTO log = new LogDTO();
        log.setRequestDate(DateUtils.formatDateToString(new Date()));
        log.setRuc(transaction.getDocIdentidad_Nro());
        log.setBusinessName(transaction.getSN_RazonSocial());

        String signerName = ISignerConfig.SIGNER_PREFIX + transaction.getDocIdentidad_Nro();

        boolean isContingencia = transaction.getTransactionContractDocRefListDTOS().stream()
                .anyMatch(map -> VALOR_CONTINGENCIA.equalsIgnoreCase(map.get("cu31")));

        ValidationHandler validationHandler = ValidationHandler.newInstance(this.docUUID);
        validationHandler.checkBasicInformation(transaction.getDOC_Id(), transaction.getDocIdentidad_Nro(),
                transaction.getDOC_FechaEmision(), transaction.getSN_EMail(), transaction.getEMail(), isContingencia);

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
                File.separator + "anexo" + File.separator + fechaEmision.get(Calendar.YEAR) +
                File.separator + (fechaEmision.get(Calendar.MONTH) + 1) + File.separator + fechaEmision.get(Calendar.DAY_OF_MONTH) +
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
            return Uni.createFrom().failure(new NullPointerException(IVenturaError.ERROR_457.getMessage()));
        }

        ConfigData config = createConfigData(client);
        log.setThirdPartyServiceInvocationDate(DateUtils.formatDateToString(new Date()));

        byte[] pdfBorradorBytes = null;
        try {
            String attachmentPathPDF = UtilsFile.getAttachmentPath(transaction, "09", applicationProperties.getRutaBaseDocAnexos());
            String documentNamePDF = DocumentNameUtils.getDocumentName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id(), "15");
            pdfBorradorBytes = documentFormatInterface.createPDFDocument(wrp, transaction, config);
            UtilsFile.storeDocumentInDisk(pdfBorradorBytes, documentNamePDF + "_borrador", "pdf", attachmentPathPDF);
        } catch (Exception e) {
            logger.error("Error al generar PDF borrador: " + e.getMessage());
        }

        Uni<TransaccionRespuesta> resultado;
        if (ESTADO_NUEVO.equalsIgnoreCase(transaction.getFE_Estado())) {
            resultado = processEstadoNReactive(transaction, config, fileHandler, signedDocument, signedXml, documentName, wrp);
        } else if (ESTADO_CONTINGENCIA.equalsIgnoreCase(transaction.getFE_Estado())) {
            resultado = processEstadoCReactive(transaction, config, fileHandler, signedDocument, documentName, wrp);
        } else {
            resultado = Uni.createFrom().failure(new IllegalStateException("Estado no reconocido: " + transaction.getFE_Estado()));
        }

        String digestValue = generateDigestValue(wrp.getAdviceType().getUBLExtensions());
        String barcodeValue = generateGuiaBarcodeInfoV2(wrp.getAdviceType().getID().getValue(),
                IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE, transaction.getDOC_FechaVencimiento(),
                BigDecimal.ZERO, BigDecimal.ZERO,
                wrp.getAdviceType().getDespatchSupplierParty(), wrp.getAdviceType().getDeliveryCustomerParty(),
                wrp.getAdviceType().getUBLExtensions());

        byte[] finalPdfBorradorBytes = pdfBorradorBytes;

        return resultado.map(trxResp -> {
            trxResp.setDigestValue(digestValue);
            trxResp.setBarcodeValue(barcodeValue);
            trxResp.setIdentificador(documentName);
            if (config.getPdfBorrador() != null && config.getPdfBorrador().equals("true")) {
                trxResp.setPdfBorrador(finalPdfBorradorBytes);
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
            if (trxResp.getZip() != null) {
                try {
                    UtilsFile.storeDocumentInDisk(trxResp.getZip(), documentName, "zip", attachmentPath);
                } catch (Exception e) {
                    logger.error("Error al guardar el ZIP del CDR: " + e.getMessage());
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

    public Uni<TransaccionRespuesta> processEstadoNReactive(TransacctionDTO tx, ConfigData config, FileHandler fileHandler,
                                                             File signedDoc, byte[] docBytes, String docName, UBLDocumentWRP wrp) {
        return guiaTicketRepo.findGuiaTicketByRucEmisorAndFeId(tx.getDocIdentidad_Nro(), tx.getFE_Id())
                .onItem().ifNull().continueWith(EMPTY_TICKET)
                .chain(existingTicket -> {
                    if (existingTicket != EMPTY_TICKET) {
                        logger.info("Ticket previo encontrado RUC={} FE_Id={}: ticket={}", tx.getDocIdentidad_Nro(), tx.getFE_Id(), existingTicket.getTicketSunat());
                        return consultarYProcesar(existingTicket.getTicketSunat(), tx, config, wrp, fileHandler, signedDoc, docName);
                    }

                    logger.info("No se encontró ticket previo para RUC={} FE_Id={}", tx.getDocIdentidad_Nro(), tx.getFE_Id());

                    return Uni.createFrom().item(Unchecked.supplier(() -> getJwtSunat(config)))
                            .chain(token -> Uni.createFrom().item(Unchecked.supplier(() -> declareSunat(docName, docBytes, token.getAccess_token()))))
                            .invoke(resp -> {
                                logger.info("Nuevo ticket creado en SUNAT: {}", resp.getNumTicket());
                                saveTicketRest(tx, resp);
                            })
                            .chain(resp -> consultarTicketConReintentos(resp.getNumTicket(), config))
                            .chain(respFinal -> manejarRespuestaSunatReactive(respFinal, wrp, fileHandler, docName, tx, config, signedDoc));
                })
                .ifNoItem().after(Duration.ofSeconds(20)).fail()
                .onFailure().recoverWithUni(ex -> {
                    logger.error("Error procesando FE_Id={} doc={}-{}-{}: {}",
                            tx.getFE_Id(), tx.getDOC_Codigo(), tx.getDOC_Serie(), tx.getDOC_Numero(), obtenerMensajeControlado(ex));
                    TransaccionRespuesta respuesta = new TransaccionRespuesta();
                    respuesta.setMensaje(obtenerMensajeControlado(ex));
                    return Uni.createFrom().item(respuesta);
                });
    }

    private String obtenerMensajeControlado(Throwable ex) {
        Throwable real = (ex.getCause() != null) ? ex.getCause() : ex;
        if (real instanceof TimeoutException) return "SUNAT no respondió dentro del tiempo esperado";
        String msg = real.getMessage();
        if (msg != null && msg.contains("504 Gateway Time-out")) return "SUNAT no disponible temporalmente (504 Gateway Time-out)";
        if (msg != null && msg.contains("503 Service Temporarily Unavailable")) return "SUNAT no disponible temporalmente (503)";
        if (msg != null && msg.contains("500 Internal Server Error")) return "SUNAT presentó un error interno (500)";
        if (msg != null && msg.contains("Unexpected character ('<'")) return "SUNAT devolvió HTML en lugar de JSON";
        return "Error consultando SUNAT: " + (msg != null ? msg : real.getClass().getSimpleName());
    }

    public Uni<TransaccionRespuesta> manejarRespuestaSunatReactive(ResponseDTO responseDTO, UBLDocumentWRP wrp,
                                                                    FileHandler fileHandler, String docName,
                                                                    TransacctionDTO tx, ConfigData config, File signedDoc) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            TransaccionRespuesta respuesta = generateResponseRest(wrp, responseDTO);
            if ("0".equals(responseDTO.getCodRespuesta())) {
                byte[] zipBytes = Base64.decodeBase64(responseDTO.getArcCdr());
                respuesta.setZip(zipBytes);
                respuesta.setTicketRest(responseDTO.getNumTicket());
                config.setUrlGuias(SunatResponseUtils.proccessResponseUrlPdfGuia(zipBytes));
            }
            try {
                byte[] pdf = processorCoreInterface.processCDRResponseContigencia(null, fileHandler, docName, tx.getDOC_Codigo(), wrp, tx, config);
                respuesta.setPdf(pdf);
            } catch (Exception pdfEx) {
                logger.error("Error generando PDF para guía {} — sin PDF: {}", docName, pdfEx.getMessage());
            }
            respuesta.setXml(fileHandler.convertFileToBytes(signedDoc));
            return respuesta;
        })).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    public Uni<TransaccionRespuesta> consultarYProcesar(String ticket, TransacctionDTO tx, ConfigData config,
                                                         UBLDocumentWRP wrp, FileHandler fileHandler,
                                                         File signedDoc, String docName) {
        return Uni.createFrom().item(Unchecked.supplier(() -> consultarTicketEnSunat(ticket, config)))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .chain(responseDTO -> {
                    updateTicketStatusFromConsulta(tx, responseDTO);
                    return manejarRespuestaSunatReactive(responseDTO, wrp, fileHandler, docName, tx, config, signedDoc);
                });
    }

    public Uni<ResponseDTO> consultarTicketConReintentos(String ticket, ConfigData config) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            ResponseDTO jwt = getJwtSunat(config);
            Thread.sleep(5000);
            ResponseDTO resp = consult(ticket, jwt.getAccess_token());
            int retries = 0;
            while ("98".equals(resp.getCodRespuesta()) && retries < 4) {
                Thread.sleep(5000);
                jwt = getJwtSunat(config);
                resp = consult(ticket, jwt.getAccess_token());
                retries++;
            }
            return resp;
        }))
        .onFailure().transform(e -> new RuntimeException("Error consultando ticket SUNAT", e))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    public void updateTicketStatusFromConsulta(TransacctionDTO transaccion, ResponseDTO responseDTO) {
        guiaTicketRepo.findGuiaTicketByRucEmisorAndFeId(transaccion.getDocIdentidad_Nro(), transaccion.getFE_Id())
                .chain(existing -> {
                    if (existing == null) {
                        logger.warn("No se encontró ticket para actualizar.");
                        return Uni.createFrom().nullItem();
                    }
                    String cod = responseDTO.getCodRespuesta();
                    if ("99".equals(cod)) {
                        return guiaTicketRepo.delete(existing)
                                .invoke(v -> logger.info("Ticket eliminado tras codRespuesta 99"));
                    } else {
                        if ("98".equals(cod)) existing.setEstadoTicket("PROCESO");
                        else if ("0".equals(cod)) existing.setEstadoTicket("APROBADO");
                        existing.setCreadoEn(DateUtils.formatDateToString(new Date()));
                        return guiaTicketRepo.save(existing)
                                .invoke(v -> logger.info("Ticket actualizado a estado: " + existing.getEstadoTicket()));
                    }
                })
                .subscribe().with(v -> {}, e -> logger.error("Error actualizando ticket: {}", e.getMessage()));
    }

    public Uni<TransaccionRespuesta> processEstadoCReactive(TransacctionDTO tx, ConfigData config, FileHandler fileHandler,
                                                             File signedDoc, String docName, UBLDocumentWRP wrp) {
        return guiaTicketRepo.findGuiaTicketByRucEmisorAndFeId(tx.getDocIdentidad_Nro(), tx.getFE_Id())
                .onItem().ifNull().continueWith(new GuiaTicket())
                .chain(ticket -> {
                    if (ticket.getTicketSunat() == null || ticket.getTicketSunat().isEmpty()) {
                        logger.warn("No se encontró ticket previo para RUC: {} FE_Id: {}", tx.getDocIdentidad_Nro(), tx.getFE_Id());
                        return Uni.createFrom().item(new TransaccionRespuesta());
                    }
                    return Uni.createFrom().item(Unchecked.supplier(() -> {
                        ResponseDTO responseDTO = consultarTicketEnSunat(ticket.getTicketSunat(), config);
                        updateTicketStatusFromConsulta(tx, responseDTO);
                        TransaccionRespuesta response = manejarRespuestaSunat(responseDTO, wrp, fileHandler, docName, tx, config);
                        if (response != null) {
                            response.setXml(fileHandler.convertFileToBytes(signedDoc));
                        }
                        return response != null ? response : new TransaccionRespuesta();
                    })).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
                });
    }

    public ResponseDTO consult(String numTicket, String token) throws JsonProcessingException {
        HttpResponse<String> response = Unirest.get("https://api-cpe.sunat.gob.pe/v1/contribuyente/gem/comprobantes/envios/" + numTicket)
                .header("Authorization", "Bearer " + token)
                .asString();
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseDTO responseDTO = objectMapper.readValue(response.body(), new TypeReference<ResponseDTO>() {});
        responseDTO.setStatusCode(response.getStatus());
        responseDTO.setNumTicket(numTicket);
        return responseDTO;
    }

    public ResponseDTO declareSunat(String documentName, byte[] fileContent, String token) throws IOException {
        byte[] zipBytes = compressToZip(fileContent, documentName + ".xml");
        String fileTo64 = zipDocumentTo64(zipBytes);
        String fileTo256Sha = zipDocumentTo265Sha(zipBytes);
        String nombreArchivo = documentName + ".zip";

        HttpResponse<String> response = Unirest.post("https://api-cpe.sunat.gob.pe/v1/contribuyente/gem/comprobantes/" + documentName)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body("{\"archivo\":{\"nomArchivo\":\"" + nombreArchivo + "\",\"arcGreZip\":\"" + fileTo64 + "\",\"hashZip\":\"" + fileTo256Sha + "\"}}")
                .asString();

        ObjectMapper objectMapper = new ObjectMapper();
        ResponseDTO responseDTO = objectMapper.readValue(response.body(), new TypeReference<ResponseDTO>() {});
        responseDTO.setStatusCode(response.getStatus());
        return responseDTO;
    }

    private byte[] compressToZip(byte[] fileContent, String internalFileName) throws IOException {
        if (fileContent == null || fileContent.length == 0) return new byte[0];
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry(internalFileName));
            zos.write(fileContent, 0, fileContent.length);
            zos.closeEntry();
            zos.finish();
            return baos.toByteArray();
        }
    }

    public String zipDocumentTo265Sha(byte[] documentContent) {
        if (documentContent == null || documentContent.length == 0) return "";
        return DigestUtils.sha256Hex(documentContent);
    }

    public String zipDocumentTo64(byte[] fileContent) {
        if (fileContent == null || fileContent.length == 0) return null;
        return new String(Base64.encodeBase64(fileContent));
    }

    public ResponseDTO getJwtSunat(ConfigData configuracion) throws JsonProcessingException {
        HttpResponse<String> response = Unirest.post("https://api-seguridad.sunat.gob.pe/v1/clientessol/" + configuracion.getClientId() + "/oauth2/token/")
                .header("Content-Type", "application/x-www-form-urlencoded")
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
        if (response.getStatus() == 400) {
            ResponseDTOAuth responseDTO400 = objectMapper.readValue(response.body(), new TypeReference<ResponseDTOAuth>() {});
            responseDTO.setResponseDTO400(responseDTO400);
        } else {
            responseDTO = objectMapper.readValue(response.body(), new TypeReference<ResponseDTO>() {});
        }
        responseDTO.setStatusCode(response.getStatus());
        return responseDTO;
    }

    public TransaccionRespuesta generateResponseRest(UBLDocumentWRP documentWRP, ResponseDTO responseDTO) {
        TransaccionRespuesta.Sunat sunatResponse = new TransaccionRespuesta.Sunat();
        sunatResponse.setId(documentWRP.getTransaccion().getDOC_Serie() + "-" + documentWRP.getTransaccion().getDOC_Numero());
        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();

        if (responseDTO.getStatusCode() == 400 || responseDTO.getStatusCode() == 401) {
            transactionResponse.setMensaje(responseDTO.getResponseDTO400() != null
                    ? responseDTO.getResponseDTO400().getError() + " - " + responseDTO.getResponseDTO400().getError_description()
                    : "Error no especificado");
        } else if (responseDTO.getStatusCode() == 404) {
            transactionResponse.setMensaje("El número de ticket para consultar no existe");
            sunatResponse.setAceptado(false);
            transactionResponse.setSunat(sunatResponse);
        } else if (responseDTO.getCodRespuesta() != null) {
            switch (responseDTO.getCodRespuesta()) {
                case "0":
                    transactionResponse.setMensaje("0 - Documento aprobado");
                    transactionResponse.setZip(Base64.decodeBase64(responseDTO.getArcCdr()));
                    sunatResponse.setAceptado(true);
                    transactionResponse.setSunat(sunatResponse);
                    break;
                case "98":
                    transactionResponse.setMensaje("98 - Documento en proceso, volver a consultar.");
                    sunatResponse.setAceptado(false);
                    transactionResponse.setSunat(sunatResponse);
                    break;
                case "99":
                    if ("1".equals(responseDTO.getIndCdrGenerado())) {
                        transactionResponse.setZip(Base64.decodeBase64(responseDTO.getArcCdr()));
                    }
                    transactionResponse.setMensaje(responseDTO.getError().getNumError() + " - " + responseDTO.getError().getDesError());
                    sunatResponse.setAceptado(false);
                    transactionResponse.setSunat(sunatResponse);
                    break;
            }
        }
        return transactionResponse;
    }

    public void saveTicketRest(TransacctionDTO transaccion, ResponseDTO responseDTO) {
        guiaTicketRepo.findGuiaTicketByRucEmisorAndFeId(transaccion.getDocIdentidad_Nro(), transaccion.getFE_Id())
                .chain(existing -> {
                    if (existing != null) {
                        logger.warn("Ya existe ticket para esta guía, no se vuelve a guardar.");
                        return Uni.createFrom().item(existing);
                    }
                    GuiaTicket guiaTicket = new GuiaTicket();
                    guiaTicket.setRucEmisor(transaccion.getDocIdentidad_Nro());
                    guiaTicket.setFeId(transaccion.getFE_Id());
                    guiaTicket.setTicketSunat(responseDTO.getNumTicket());
                    guiaTicket.setCreadoEn(DateUtils.formatDateToString(new Date()));
                    return guiaTicketRepo.save(guiaTicket);
                })
                .subscribe().with(v -> {}, e -> logger.error("Error guardando ticket: {}", e.getMessage()));
    }

    private ResponseDTO consultarTicketEnSunat(String ticket, ConfigData configuracion) throws JsonProcessingException {
        ResponseDTO responseDTOJWT = getJwtSunat(configuracion);
        ResponseDTO responseDTO = consult(ticket, responseDTOJWT.getAccess_token());
        if (responseDTO.getStatusCode() == 401) {
            responseDTOJWT = getJwtSunat(configuracion);
            responseDTO = consult(ticket, responseDTOJWT.getAccess_token());
        }
        return responseDTO;
    }

    private TransaccionRespuesta manejarRespuestaSunat(ResponseDTO responseDTO, UBLDocumentWRP documentWRP,
                                                        FileHandler fileHandler, String documentName,
                                                        TransacctionDTO transaction, ConfigData configuracion) {
        if (responseDTO.getStatusCode() == 400 || responseDTO.getStatusCode() == 404) {
            return generateResponseRest(documentWRP, responseDTO);
        }

        TransaccionRespuesta transactionResponse = null;
        if (responseDTO.getCodRespuesta() != null) {
            switch (responseDTO.getCodRespuesta()) {
                case "0":
                    transactionResponse = generateResponseRest(documentWRP, responseDTO);
                    configuracion.setUrlGuias(SunatResponseUtils.proccessResponseUrlPdfGuia(Base64.decodeBase64(responseDTO.getArcCdr())));
                    transactionResponse.setTicketRest(responseDTO.getNumTicket());
                    break;
                case "98":
                case "99":
                default:
                    transactionResponse = generateResponseRest(documentWRP, responseDTO);
                    break;
            }

            if (transactionResponse != null) {
                try {
                    byte[] pdf = processorCoreInterface.processCDRResponseContigencia(null, fileHandler,
                            documentName, transaction.getDOC_Codigo(), documentWRP, transaction, configuracion);
                    transactionResponse.setPdf(pdf);
                } catch (PDFReportException e) {
                    transactionResponse.setErrorPdf(e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
        return transactionResponse;
    }
}
