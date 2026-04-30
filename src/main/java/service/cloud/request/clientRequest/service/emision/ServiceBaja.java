package service.cloud.request.clientRequest.service.emision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import service.cloud.request.clientRequest.config.ApplicationProperties;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.estela.dto.BajaData;
import service.cloud.request.clientRequest.estela.dto.FileRequestDTO;
import service.cloud.request.clientRequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientRequest.estela.service.DocumentBajaQueryService;
import service.cloud.request.clientRequest.estela.service.DocumentBajaService;
import service.cloud.request.clientRequest.extras.ISignerConfig;
import service.cloud.request.clientRequest.extras.ISunatConnectorConfig;
import service.cloud.request.clientRequest.handler.FileHandler;
import service.cloud.request.clientRequest.handler.UBLDocumentHandler;
import service.cloud.request.clientRequest.handler.document.DocumentNameHandler;
import service.cloud.request.clientRequest.handler.document.SignerHandler;
import service.cloud.request.clientRequest.model.Client;
import service.cloud.request.clientRequest.mongo.model.Counter;
import service.cloud.request.clientRequest.mongo.model.LogDTO;
import service.cloud.request.clientRequest.mongo.model.TransaccionBaja;
import service.cloud.request.clientRequest.mongo.repo.ITransaccionBajaRepository;
import service.cloud.request.clientRequest.proxy.model.CdrStatusResponse;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceBaja;
import service.cloud.request.clientRequest.utils.JsonUtils;
import service.cloud.request.clientRequest.utils.SunatResponseUtils;
import service.cloud.request.clientRequest.utils.exception.DateUtils;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.utils.files.CertificateUtils;
import service.cloud.request.clientRequest.utils.files.DocumentConverterUtils;
import service.cloud.request.clientRequest.utils.files.UtilsFile;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.summarydocuments_1.SummaryDocumentsType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.voideddocuments_1.VoidedDocumentsType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * -----------------------------------------------------------------------------

 * Proyecto          : facturación SAAS

 *                     conforme a las especificaciones de SUNAT.
 *
 * Autor             : Jose Luis Becerra
 * Rol               : Software Developer Senior
 * Fecha de creación : 09/07/2025
 * -----------------------------------------------------------------------------
 */

@ApplicationScoped
public class ServiceBaja implements IServiceBaja {

    private static final Logger logger = LoggerFactory.getLogger(ServiceBaja.class);

    private static final TransaccionBaja EMPTY_BAJA = new TransaccionBaja();

    @Inject private ClientProperties clientProperties;
    @Inject private ApplicationProperties applicationProperties;
    @Inject private ITransaccionBajaRepository iTransaccionBajaRepository;
    @Inject private DocumentBajaQueryService documentBajaQueryService;
    @Inject private DocumentBajaService documentBajaService;

    private static final String docUUID = "123123";

    @Override
    public Mono<TransaccionRespuesta> transactionVoidedDocument(TransacctionDTO transaction, String doctype) {
        if (transaction == null) {
            return Mono.error(new IllegalArgumentException("La transacción no puede ser nula."));
        }

        LogDTO log = inicializarLog(transaction);
        String attachmentPath = UtilsFile.getAttachmentPath(transaction, doctype, applicationProperties.getRutaBaseDocAnexos());
        FileHandler fileHandler = FileHandler.newInstance(docUUID);
        fileHandler.setBaseDirectory(attachmentPath);

        Client client = clientProperties.listaClientesOf(transaction.getDocIdentidad_Nro());
        if (client == null) {
            return Mono.just(mensajeError("Cliente no encontrado.", new TransaccionRespuesta()));
        }
        ConfigData config = createConfigData(client);

        return findByRucEmpresaAndDocIdReactive(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id())
                .defaultIfEmpty(EMPTY_BAJA)
                .flatMap(transBaja -> {
                    if (transBaja == EMPTY_BAJA) {
                        logger.info("No existe TransaccionBaja previa, se generará una nueva para el documento {}-{}",
                                transaction.getDOC_Serie(), transaction.getDOC_Numero());
                    } else {
                        logger.info("TransaccionBaja previa encontrada: {} (ticket: {})",
                                transBaja.getSerie(), transBaja.getTicketBaja());
                    }
                    FileRequestDTO soapRequest = buildSoapRequest(transaction, client, config);
                    return obtenerOTramitarTicketReactive(transBaja, transaction, client, fileHandler, config, soapRequest, attachmentPath)
                            .flatMap(bajaData -> {
                                soapRequest.setTicket(bajaData.getTicket());
                                log.setThirdPartyServiceInvocationDate(DateUtils.formatDateToString(new Date()));

                                // Consulta a SUNAT/OSE con reintentos
                                return consultarEstadoConReintentos(soapRequest)
                                        .flatMap(sunatResponse -> {
                                            CdrStatusResponse cdrResponse = new CdrStatusResponse();
                                            TransaccionRespuesta response = new TransaccionRespuesta();
                                            log.setThirdPartyServiceResponseDate(DateUtils.formatDateToString(new Date()));

                                            if (sunatResponse == null) {
                                                return Mono.just(mensajeError("Error al consultar el estado del ticket.", response));
                                            }
                                            cdrResponse.setContent(sunatResponse.getContent());
                                            cdrResponse.setStatusMessage(sunatResponse.getMessage());

                                            logger.info("Ticket baja: {}| Ruc-Tipo-Serie-Correlativo: {}-{}-{}-{}", bajaData.getTicket(),
                                                    transaction.getDocIdentidad_Nro(), transaction.getDOC_Codigo(),
                                                    transaction.getDOC_Serie(), transaction.getDOC_Numero());

                                            String documentName = bajaData.getNameBaja();

                                            if (cdrResponse.getContent() != null) {
                                                response = processOseResponseBAJA(cdrResponse.getContent(), transaction, documentName, config, attachmentPath);
                                            } else {
                                                response.setMensaje(cdrResponse.getStatusMessage());
                                            }
                                            response.setIdentificador(documentName);
                                            response.setTicketRest(bajaData.getTicket());
                                            completarLog(log, response, transaction, attachmentPath);
                                            response.setLogDTO(log);

                                            return Mono.just(response);

                                        })
                                        .onErrorResume(ex -> {
                                            logger.error("Error en transactionVoidedDocument", ex);
                                            TransaccionRespuesta errResp = new TransaccionRespuesta();
                                            errResp.setMensaje("Ocurrió un error en el proceso de anulación: " + ex.getMessage());
                                            completarLog(log, errResp, transaction, attachmentPath);
                                            errResp.setLogDTO(log);

                                            return Mono.just(errResp);
                                        });
                            });
                });
    }

    public Mono<TransaccionBaja> findByRucEmpresaAndDocIdReactive(String rucEmpresa, String docId) {
        // método del repositorio es reactivo
        return iTransaccionBajaRepository.findFirstByRucEmpresaAndDocId(rucEmpresa, docId);
    }

    public Mono<BajaData> obtenerOTramitarTicketReactive(
            TransaccionBaja transBaja,
            TransacctionDTO tx,
            Client client,
            FileHandler fileHandler,
            ConfigData config,
            FileRequestDTO soapRequest,
            String attachmentPath) {

        // 1. Si ya existe ticket guardado, retorna de inmediato.
        if (transBaja != null && transBaja.getTicketBaja() != null && !transBaja.getTicketBaja().isEmpty()) {
            BajaData bajaData = new BajaData();
            bajaData.setTicket(transBaja.getTicketBaja());
            bajaData.setNameBaja(transBaja.getSerie());
            return Mono.just(bajaData);
        }

        // 2. Validación de comentario requerido.
        if (tx.getFE_Comentario() == null || tx.getFE_Comentario().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Ingresar razón de anulación, y colocar APROBADO y volver a consultar."));
        }

        // *** FLUJO SEGURO ***
        return generarIDyFecha(tx)
                .flatMap(transaccionBaja ->
                        Mono.fromCallable(() -> {
                            // ---- ARMADO DE DOCUMENTO Y PREPARACIÓN ----
                            tx.setANTICIPO_Id(transaccionBaja.getSerie());
                            String certPath = applicationProperties.getRutaBaseDocConfig() + tx.getDocIdentidad_Nro() + File.separator + client.getCertificadoName();
                            byte[] certificado = CertificateUtils.loadCertificate(certPath);
                            CertificateUtils.validateCertificate(certificado, client.getCertificadoPassword(), applicationProperties.getSupplierCertificate(), applicationProperties.getKeystoreCertificateType());

                            String signerName = ISignerConfig.SIGNER_PREFIX + tx.getDocIdentidad_Nro();
                            SignerHandler signer = SignerHandler.newInstance();
                            signer.setConfiguration(certificado, client.getCertificadoPassword(), applicationProperties.getKeystoreCertificateType(), applicationProperties.getSupplierCertificate(), signerName);

                            UBLDocumentHandler ublHandler = UBLDocumentHandler.newInstance(docUUID);
                            String docNameRaw = DocumentNameHandler.getInstance().getVoidedDocumentName(tx.getDocIdentidad_Nro(), tx.getANTICIPO_Id());
                            String docNameFinal = tx.getDOC_Serie().startsWith("B") ? docNameRaw.replace("RA", "RC") : docNameRaw;
                            byte[] xmlDocument;

                            if (tx.getDOC_Serie().startsWith("B") || tx.getDOC_Codigo().equals("03")) {
                                SummaryDocumentsType summary = ublHandler.generateSummaryDocumentsTypeV2(tx, signerName);
                                xmlDocument = DocumentConverterUtils.convertDocumentToBytes(summary);
                                fileHandler.storeDocumentInDisk(summary, docNameFinal);
                            } else {
                                VoidedDocumentsType voided = ublHandler.generateVoidedDocumentType(tx, signerName);
                                xmlDocument = DocumentConverterUtils.convertDocumentToBytes(voided);
                                fileHandler.storeDocumentInDisk(voided, docNameFinal);
                            }

                            // Firmar y almacenar
                            byte[] signed = signer.signDocumentv2(xmlDocument, docUUID);
                            UtilsFile.storeDocumentInDisk(signed, docNameFinal, "xml", attachmentPath);

                            // Comprimir y codificar
                            byte[] zipBytes = compressUBLDocumentv2(signed, docNameFinal + ".xml");
                            soapRequest.setContentFile(convertToBase64(zipBytes));

                            // IMPORTANTE: ahora sí, con nombre correcto
                            soapRequest.setFileName(DocumentNameHandler.getInstance().getZipName(docNameFinal));

                            // Preparamos un registro completamente NUEVO
                            TransaccionBaja nuevoRegistro = new TransaccionBaja();
                            nuevoRegistro.setRucEmpresa(transaccionBaja.getRucEmpresa());
                            nuevoRegistro.setFecha(transaccionBaja.getFecha());
                            nuevoRegistro.setIdd(transaccionBaja.getIdd());
                            nuevoRegistro.setSerie(transaccionBaja.getSerie());
                            nuevoRegistro.setDocId(transaccionBaja.getDocId());
                            nuevoRegistro.setFechaHora(LocalDateTime.now());

                            return new Object[] {nuevoRegistro, soapRequest, docNameFinal};
                        }).subscribeOn(Schedulers.boundedElastic())
                )
                .flatMap(arr -> {
                    TransaccionBaja nuevoRegistro = (TransaccionBaja) arr[0];
                    FileRequestDTO soapReq = (FileRequestDTO) arr[1];

                    // Enviar a SUNAT
                    return documentBajaService.processBajaRequest(soapReq.getService(), soapReq)
                            .flatMap(fileResponse -> {
                                if (fileResponse == null || fileResponse.getTicket() == null) {
                                    return Mono.error(new IllegalStateException("No se recibió ticket de SUNAT."));
                                }
                                // Guardar el ticket recién recibido
                                nuevoRegistro.setTicketBaja(fileResponse.getTicket());
                                // Guardar SIEMPRE un registro nuevo
                                nuevoRegistro.setId(null);
                                return iTransaccionBajaRepository.save(nuevoRegistro)
                                        .then(Mono.fromCallable(() -> {
                                            BajaData bajaData = new BajaData();
                                            bajaData.setTicket(fileResponse.getTicket());
                                            bajaData.setNameBaja(nuevoRegistro.getSerie());
                                            return bajaData;
                                        }));
                            });
                });
    }

    private LogDTO inicializarLog(TransacctionDTO tx) {
        LogDTO log = new LogDTO();
        log.setRequestDate(DateUtils.formatDateToString(new Date()));
        log.setRuc(tx.getDocIdentidad_Nro());
        log.setBusinessName(tx.getRazonSocial());
        return log;
    }

    private TransaccionRespuesta mensajeError(String msg, TransaccionRespuesta response) {
        response.setMensaje(msg);
        return response;
    }

    private FileRequestDTO buildSoapRequest(TransacctionDTO tx, Client client, ConfigData config) {
        FileRequestDTO request = new FileRequestDTO();
        String serviceUrl = applicationProperties.obtenerUrl(
                client.getIntegracionWs(), tx.getFE_Estado(), tx.getFE_TipoTrans(), tx.getDOC_Codigo()
        );
        request.setService(serviceUrl);
        request.setUsername(config.getUsuarioSol());
        request.setPassword(config.getClaveSol());
        //request.setFileName(DocumentNameHandler.getInstance().getZipName(""));
        return request;
    }

    private void completarLog(LogDTO log, TransaccionRespuesta txResp, TransacctionDTO tx, String path) {
        String docName = txResp.getIdentificador();
        log.setPathThirdPartyRequestXml(path + "\\" + docName + ".xml");
        log.setPathThirdPartyResponseXml(path + "\\" + docName + ".zip");
        log.setPathBase(path + "\\" + docName + ".json");
        log.setObjectTypeAndDocEntry(tx.getFE_ObjectType() + " - " + tx.getFE_DocEntry());
        log.setSeriesAndCorrelative(docName);
        log.setResponse((JsonUtils.toJson(txResp.getSunat())).equals("null") ? txResp.getMensaje() : JsonUtils.toJson(txResp.getSunat()));
        log.setResponseDate(DateUtils.formatDateToString(new Date()));
    }

    // Mantén los métodos adicionales ya existentes como consultarEstadoConReintentos(), generarIDyFecha(), etc.
    private ConfigData createConfigData(Client client) {
        return ConfigData.builder()
                .usuarioSol(client.getUsuarioSol())
                .claveSol(client.getClaveSol())
                .integracionWs(client.getIntegracionWs())
                .ambiente(applicationProperties.getAmbiente())
                .pdfBorrador(client.getPdfBorrador())
                .impresionPDF(client.getImpresion())
                .rutaBaseDoc(applicationProperties.getRutaBaseDocAnexos())
                .rutaBaseConfig(applicationProperties.getRutaBaseDocConfig())
                .build();
    }

    public Mono<FileResponseDTO> consultarEstadoConReintentos(FileRequestDTO soapRequest) {
        return Mono.defer(() ->
                Mono.delay(Duration.ofSeconds(5))
                        .flatMap(t -> documentBajaQueryService.processAndSaveFile(soapRequest.getService(), soapRequest))
                        .flatMap(response -> {
                            if (!response.getMessage().contains("98")) {
                                return Mono.just(response);
                            } else {
                                return Mono.error(new IllegalStateException("SUNAT  aún está procesando (código 98)"));
                            }
                        })
        ).retryWhen(
                Retry.fixedDelay(5, Duration.ofSeconds(5))
                        .filter(ex -> ex.getMessage().contains("98"))
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                new RuntimeException("SUNAT no procesó el ticket después de varios intentos."))
        );
    }

    private TransaccionRespuesta processOseResponseBAJA(byte[] statusResponse, TransacctionDTO transaction, String documentName, ConfigData configuracion, String attachmentPath) {
        TransaccionRespuesta.Sunat sunatResponse = SunatResponseUtils.proccessResponse(statusResponse, transaction, configuracion.getIntegracionWs());//proccessResponse(statusResponse, transaction, configuracion.getIntegracionWs());
        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
        if ((IVenturaError.ERROR_0.getId() == sunatResponse.getCodigo()) || (4000 <= sunatResponse.getCodigo())) {

            /**se realiza el anexo del documento de baja*/
            if (null != statusResponse && 0 < statusResponse.length) {
                UtilsFile.storePDFDocumentInDisk(statusResponse, attachmentPath, documentName + "_SUNAT_CDR_BAJA", ISunatConnectorConfig.EE_ZIP);//fileHandler.storePDFDocumentInDisk(statusResponse, documentName + "_SUNAT_CDR_BAJA", ISunatConnectorConfig.EE_ZIP);
            }

            String mensaje = sunatResponse.getMensaje();

            if (mensaje != null) {
                mensaje = mensaje.replace("La Comunicacion de baja", "La Comunicación de Baja");
                mensaje = mensaje.replace("El Resumen diario", "El Resumen de Boletas Baja" );
                mensaje = mensaje.replace("El resumen de reversion", "La Comunicación de Baja");
            }

            transactionResponse.setMensaje(mensaje);
            transactionResponse.setZip(statusResponse);

        } else {
            transactionResponse.setMensaje(sunatResponse.getMensaje());
            transactionResponse.setZip(statusResponse);
        }

        return transactionResponse;
    }

    public Mono<TransaccionBaja> generarIDyFecha(TransacctionDTO tr) {
        String fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefijo;
        if (Arrays.asList("20", "40").contains(tr.getDOC_Codigo())) {
            prefijo = "RR";
        } else if (tr.getDOC_Codigo().equals("03") || tr.getDOC_Serie().startsWith("B")) {
            prefijo = "RC";
        } else {
            prefijo = "RA";
        }
        String ruc = tr.getDocIdentidad_Nro();
        return getNextSequence(ruc, fechaActual, prefijo)
                .map(seq -> {
                    String correlativo = String.format("%05d", seq);
                    String serie = prefijo + "-" + fechaActual + "-" + correlativo;
                    TransaccionBaja baja = new TransaccionBaja();
                    baja.setRucEmpresa(ruc);
                    baja.setFecha(fechaActual);
                    baja.setIdd(seq.intValue());
                    baja.setSerie(serie);
                    baja.setDocId(tr.getDOC_Id());
                    tr.setANTICIPO_Id(serie);
                    return baja;
                });
    }

    public Mono<Long> getNextSequence(String ruc, String fecha, String prefijo) {
        return iTransaccionBajaRepository.findFirstByRucEmpresaOrderByFechaDescIddDesc(ruc)
                .map(tb -> tb.getFecha() != null && tb.getFecha().equals(fecha) ? (long) tb.getIdd() + 1L : 1L)
                .defaultIfEmpty(1L);
    }

    private byte[] compressUBLDocumentv2(byte[] document, String documentName) throws IOException {
        byte[] zipDocument = null;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(document);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(bos)) {

            byte[] array = new byte[10000];
            int read;
            zos.putNextEntry(new ZipEntry(documentName));

            while ((read = bis.read(array)) != -1) {
                zos.write(array, 0, read);
            }

            zos.closeEntry();
            zos.finish(); // Forzar el cierre de la entrada ZIP antes de obtener los bytes
            zipDocument = bos.toByteArray();  // Devolver directamente los bytes comprimidos

        } catch (Exception e) {
            logger.error("compressUBLDocument() [" + this.docUUID + "] " + e.getMessage());
            throw new IOException(IVenturaError.ERROR_455.getMessage());
        }

        return zipDocument; // Devuelve los bytes comprimidos directamente
    }

    private String convertToBase64(byte[] content) {
        return Base64.getEncoder().encodeToString(content);
    }
}
