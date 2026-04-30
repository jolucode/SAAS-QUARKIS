package service.cloud.request.clientRequest.service.emision;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.cloud.request.clientRequest.config.ApplicationProperties;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.estela.dto.FileRequestDTO;
import service.cloud.request.clientRequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientRequest.estela.service.DocumentEmissionService;
import service.cloud.request.clientRequest.estela.service.DocumentQueryService;
import service.cloud.request.clientRequest.extras.ISignerConfig;
import service.cloud.request.clientRequest.handler.UBLDocumentHandler;
import service.cloud.request.clientRequest.handler.document.DocumentNameHandler;
import service.cloud.request.clientRequest.handler.document.SignerHandler;
import service.cloud.request.clientRequest.model.Client;
import service.cloud.request.clientRequest.mongo.model.LogDTO;
import service.cloud.request.clientRequest.service.core.DocumentFormatInterface;
import service.cloud.request.clientRequest.service.core.ProcessorCoreInterface;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceEmision;
import service.cloud.request.clientRequest.utils.*;
import service.cloud.request.clientRequest.utils.exception.DateUtils;
import service.cloud.request.clientRequest.utils.files.CertificateUtils;
import service.cloud.request.clientRequest.utils.files.DocumentConverterUtils;
import service.cloud.request.clientRequest.utils.files.DocumentNameUtils;
import service.cloud.request.clientRequest.utils.files.UtilsFile;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.creditnote_2.CreditNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.debitnote_2.DebitNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.invoice_2.InvoiceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.perception_1.PerceptionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.retention_1.RetentionType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;

@ApplicationScoped
public class ServiceEmision implements IServiceEmision {

    Logger logger = LoggerFactory.getLogger(ServiceEmision.class);

    @Inject
    ClientProperties clientProperties;

    private final String docUUID = "123123";

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    ProcessorCoreInterface processorCoreInterface;

    @Inject
    DocumentFormatInterface documentFormatInterface;

    @Inject
    DocumentEmissionService documentEmissionService;

    @Inject
    private DocumentQueryService documentQueryService;

    @Override
    public Uni<TransaccionRespuesta> transactionDocument(TransacctionDTO transaction, String doctype) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            LogDTO log = new LogDTO();
            log.setRequestDate(DateUtils.formatDateToString(new Date()));
            log.setRuc(transaction.getDocIdentidad_Nro());
            log.setBusinessName(transaction.getRazonSocial());

            boolean isContingencia = isContingencia(transaction);
            ValidationHandler validationHandler = ValidationHandler.newInstance(this.docUUID);
            validationHandler.checkBasicInformation(transaction.getDOC_Id(), transaction.getDocIdentidad_Nro(),
                    transaction.getDOC_FechaEmision(), transaction.getSN_EMail(), transaction.getEMail(), isContingencia);

            Client client = getClientData(transaction);
            UBLDocumentHandler ublHandler = UBLDocumentHandler.newInstance(this.docUUID);
            String attachmentPath = UtilsFile.getAttachmentPath(transaction, doctype, applicationProperties.getRutaBaseDocAnexos());
            String signerName = ISignerConfig.SIGNER_PREFIX + transaction.getDocIdentidad_Nro();

            byte[] xmlDocument = null;
            TransaccionRespuesta transactionResponse = new TransaccionRespuesta();

            ObjectMapper mapper = new ObjectMapper();
            TransacctionDTO transactionXML_Pre = mapper.readValue(mapper.writeValueAsBytes(transaction), TransacctionDTO.class);
            TransacctionDTO transactionXML = TextUtils.processAllTextFieldsXML(transactionXML_Pre);

            try {
                switch (doctype) {
                    case "07" -> {
                        CreditNoteType creditNoteType = ublHandler.generateCreditNoteType(transactionXML, signerName);
                        xmlDocument = DocumentConverterUtils.convertDocumentToBytes(creditNoteType);
                    }
                    case "08" -> {
                        DebitNoteType debitNoteType = ublHandler.generateDebitNoteType(transactionXML, signerName);
                        xmlDocument = DocumentConverterUtils.convertDocumentToBytes(debitNoteType);
                    }
                    case "01", "03" -> {
                        InvoiceType invoiceType = ublHandler.generateInvoiceType(transactionXML, signerName);
                        xmlDocument = DocumentConverterUtils.convertDocumentToBytes(invoiceType);
                    }
                    case "40" -> {
                        PerceptionType perceptionType = ublHandler.generatePerceptionType(transactionXML, signerName);
                        validationHandler.checkPerceptionDocument(perceptionType);
                        xmlDocument = DocumentConverterUtils.convertDocumentToBytes(perceptionType);
                    }
                    case "20" -> {
                        RetentionType retentionType = ublHandler.generateRetentionType(transactionXML, signerName);
                        validationHandler.checkRetentionDocument(retentionType);
                        xmlDocument = DocumentConverterUtils.convertDocumentToBytes(retentionType);
                    }
                }
            } catch (Exception e) {
                transactionResponse.setMensaje("error :" + e.getMessage());
            }

            String certificatePath = applicationProperties.getRutaBaseDocConfig() + transaction.getDocIdentidad_Nro() + File.separator + client.getCertificadoName();
            byte[] certificado = CertificateUtils.loadCertificate(certificatePath);
            CertificateUtils.validateCertificate(certificado, client.getCertificadoPassword(), applicationProperties.getSupplierCertificate(), applicationProperties.getKeystoreCertificateType());
            SignerHandler signerHandler = SignerHandler.newInstance();
            signerHandler.setConfiguration(certificado, client.getCertificadoPassword(), applicationProperties.getKeystoreCertificateType(), applicationProperties.getSupplierCertificate(), signerName);

            String documentName = DocumentNameUtils.getDocumentName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id(), doctype);

            if (xmlDocument != null) {
                byte[] signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);
                try {
                    UtilsFile.storeDocumentInDisk(signedXmlDocument, documentName, "xml", attachmentPath);
                } catch (IOException e) {
                    logger.error("Error al guardar el archivo: " + e.getMessage());
                }

                UBLDocumentWRP documentWRP = configureDocumentWRP(signedXmlDocument, transaction.getDOC_Codigo(), doctype);
                documentWRP.setTransaccion(transaction);
                ConfigData configuracion = createConfigData(client, transaction);

                byte[] pdfBorradorBytes = null;
                if (Boolean.parseBoolean(configuracion.getPdfBorrador())) {
                    try {
                        pdfBorradorBytes = documentFormatInterface.createPDFDocument(documentWRP, transaction, configuracion);
                        if (pdfBorradorBytes != null) {
                            UtilsFile.storeDocumentInDisk(pdfBorradorBytes, documentName + "_borrador", "pdf", attachmentPath);
                        }
                    } catch (Exception e) {
                        logger.error("Error al generar PDF borrador: " + e.getMessage());
                    }
                }

                byte[] zipBytes = DocumentConverterUtils.compressUBLDocument(signedXmlDocument, documentName + ".xml");
                String base64Content = convertToBase64(zipBytes);

                log.setThirdPartyServiceInvocationDate(DateUtils.formatDateToString(new Date()));
                transactionResponse = handleTransactionStatus(base64Content, transaction, signedXmlDocument, documentWRP, configuracion, documentName, attachmentPath);
                log.setThirdPartyServiceResponseDate(DateUtils.formatDateToString(new Date()));

                if (transactionResponse.getPdf() != null)
                    transactionResponse.setPdfBorrador(transactionResponse.getPdf());

                if (pdfBorradorBytes != null) {
                    transactionResponse.setPdfBorrador(pdfBorradorBytes);
                }

                transactionResponse.setIdentificador(documentName);
                log.setPathThirdPartyRequestXml(attachmentPath + "\\" + documentName + ".xml");
                log.setPathThirdPartyResponseXml(attachmentPath + "\\" + documentName + ".zip");
                log.setObjectTypeAndDocEntry(transaction.getFE_ObjectType() + " - " + transaction.getFE_DocEntry());
                log.setSeriesAndCorrelative(documentName);
                log.setResponse(transactionResponse.getMensaje());
                log.setResponseDate(DateUtils.formatDateToString(new Date()));
                log.setPathBase(attachmentPath + "\\" + documentName + ".json");
                transactionResponse.setLogDTO(log);
            }

            return transactionResponse;
        })).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    private String convertToBase64(byte[] content) {
        return Base64.getEncoder().encodeToString(content);
    }

    private TransaccionRespuesta handleTransactionStatus(String base64Content, TransacctionDTO transaction,
                                                         byte[] signedXmlDocument, UBLDocumentWRP documentWRP,
                                                         ConfigData configuracion, String documentName,
                                                         String attachmentPath) throws Exception {
        String estado = transaction.getFE_Estado().toUpperCase();
        TransaccionRespuesta transaccionRespuesta = null;
        if (estado.equals("N")) {
            transaccionRespuesta = processNewTransaction(base64Content, transaction, signedXmlDocument, documentWRP, configuracion, documentName, attachmentPath);
        }
        if (estado.equals("C") || (transaccionRespuesta != null && (transaccionRespuesta.getMensaje().contains("1033") || transaccionRespuesta.getMensaje().contains("fue registrado previamente")))) {
            transaccionRespuesta = processCancelledTransaction(transaction, signedXmlDocument, documentWRP, configuracion, documentName, attachmentPath);
        }
        return transaccionRespuesta;
    }

    private TransaccionRespuesta processNewTransaction(String base64Content, TransacctionDTO transaction, byte[] signedXmlDocument,
                                                       UBLDocumentWRP documentWRP, ConfigData configuracion,
                                                       String documentName, String attachmentPath) throws Exception {
        Thread.sleep(50);

        FileRequestDTO soapRequest = new FileRequestDTO();
        String urlClient = applicationProperties.obtenerUrl(configuracion.getIntegracionWs(), transaction.getFE_Estado(), transaction.getFE_TipoTrans(), transaction.getDOC_Codigo());
        soapRequest.setService(urlClient);
        soapRequest.setUsername(configuracion.getUsuarioSol());
        soapRequest.setPassword(configuracion.getClaveSol());
        soapRequest.setFileName(DocumentNameHandler.getInstance().getZipName(documentName));
        soapRequest.setContentFile(base64Content);

        FileResponseDTO responseDTO = documentEmissionService.processDocumentEmission(soapRequest.getService(), soapRequest).await().indefinitely();
        if (responseDTO.getContent() != null) {
            return processorCoreInterface.processCDRResponseV2(responseDTO.getContent(), signedXmlDocument, documentWRP, transaction, configuracion, documentName, attachmentPath);
        } else {
            return processorCoreInterface.processResponseSinCDR(documentWRP, transaction, configuracion, responseDTO);
        }
    }

    private TransaccionRespuesta processCancelledTransaction(TransacctionDTO transaction, byte[] signedXmlDocument,
                                                             UBLDocumentWRP documentWRP, ConfigData configuracion,
                                                             String documentName, String attachmentPath) throws Exception {
        FileRequestDTO soapRequest = new FileRequestDTO();
        transaction.setFE_Estado("C");
        String urlClient = applicationProperties.obtenerUrl(configuracion.getIntegracionWs(), transaction.getFE_Estado(), transaction.getFE_TipoTrans(), transaction.getDOC_Codigo());
        soapRequest.setService(urlClient);
        soapRequest.setIntegracionWs(configuracion.getIntegracionWs());
        soapRequest.setUsername(configuracion.getUsuarioSol());
        soapRequest.setPassword(configuracion.getClaveSol());
        soapRequest.setRucComprobante(transaction.getDocIdentidad_Nro());
        soapRequest.setTipoComprobante(transaction.getDOC_Codigo());
        soapRequest.setSerieComprobante(transaction.getDOC_Serie());
        soapRequest.setNumeroComprobante(transaction.getDOC_Numero());

        FileResponseDTO responseDTO = documentQueryService.processAndSaveFile(soapRequest.getService(), soapRequest).await().indefinitely();
        if (responseDTO.getContent() != null) {
            return processorCoreInterface.processCDRResponseV2(responseDTO.getContent(), signedXmlDocument, documentWRP, transaction, configuracion, documentName, attachmentPath);
        } else {
            return processorCoreInterface.processResponseSinCDR(documentWRP, transaction, configuracion, responseDTO);
        }
    }

    private ConfigData createConfigData(Client client, TransacctionDTO transacctionDTO) {
        String valorIngles = transacctionDTO.getTransactionContractDocRefListDTOS().stream()
                .map(contractMap -> contractMap.get("pdfadicional"))
                .filter(valor -> valor != null && !valor.isEmpty())
                .findFirst()
                .orElse("");

        return ConfigData.builder()
                .usuarioSol(client.getUsuarioSol())
                .claveSol(client.getClaveSol())
                .integracionWs(client.getIntegracionWs())
                .ambiente(applicationProperties.getAmbiente())
                .pdfBorrador(client.getPdfBorrador())
                .impresionPDF(client.getImpresion())
                .rutaBaseDoc(applicationProperties.getRutaBaseDocAnexos())
                .rutaBaseConfig(applicationProperties.getRutaBaseDocConfig())
                .pdfIngles(valorIngles)
                .build();
    }

    private boolean isContingencia(TransacctionDTO transaction) {
        return false;
    }

    private Client getClientData(TransacctionDTO transaction) {
        return clientProperties.listaClientesOf(transaction.getDocIdentidad_Nro());
    }

    private UBLDocumentWRP configureDocumentWRP(byte[] signedXmlDocument, String docCodigo, String doctype) {
        UBLDocumentWRP documentWRP = new UBLDocumentWRP();
        Object ublDocument = deserializeSignedDocument(signedXmlDocument, docCodigo);
        switch (doctype) {
            case "07": documentWRP.setCreditNoteType((CreditNoteType) ublDocument); break;
            case "08": documentWRP.setDebitNoteType((DebitNoteType) ublDocument); break;
            case "01": case "03": documentWRP.setInvoiceType((InvoiceType) ublDocument); break;
            case "40": documentWRP.setPerceptionType((PerceptionType) ublDocument); break;
            case "20": documentWRP.setRetentionType((RetentionType) ublDocument); break;
            default: throw new IllegalArgumentException("Invalid document type: " + doctype);
        }
        return documentWRP;
    }

    public Object deserializeSignedDocument(byte[] signedXmlDocument, String documentCode) {
        Class<?> documentClass = DocumentConfig.DOCUMENT_TYPE_MAP.get(documentCode);
        if (documentClass == null) {
            logger.error("Código de documento no reconocido: " + documentCode);
            return null;
        }
        try {
            JAXBContext jaxbContext = DocumentConfig.getJAXBContext(documentClass);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(new ByteArrayInputStream(signedXmlDocument));
        } catch (Exception e) {
            logger.error("Error deserializando documento: {}", e.getMessage(), e);
            return null;
        }
    }
}
