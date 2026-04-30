package service.cloud.request.clientRequest.handler.refactorPdf.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.encoder.PDF417;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.NodeList;
import service.cloud.request.clientRequest.dto.dto.*;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.RetentionObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.WrapperItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.item.RetentionItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.config.JasperReportConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.service.RetentionPDFGenerator;
import service.cloud.request.clientRequest.utils.DateConverter;
import service.cloud.request.clientRequest.utils.DateUtil;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionsType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1.SUNATRetentionDocumentReferenceType;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class RetentionPDFBuilder extends BaseDocumentService implements RetentionPDFGenerator {

    private static final Logger logger = Logger.getLogger(RetentionPDFBuilder.class);

    @Autowired
    private JasperReportConfig jasperReportConfig;

    String docUUID = "asd";

    @Override
    public synchronized byte[] generateRetentionPDF(UBLDocumentWRP retentionType, ConfigData configData) {
        byte[] perceptionBytes = null;

        try {

            RetentionObject retentionObject = new RetentionObject();
            retentionObject.setDocumentIdentifier(retentionType.getRetentionType().getId().getValue());
            retentionObject.setIssueDate(DateUtil.formatIssueDate(retentionType.getRetentionType().getIssueDate().getValue()));


            retentionObject.setSenderSocialReason(retentionType.getRetentionType().getAgentParty().getPartyLegalEntity().get(0).getRegistrationName().getValue().toUpperCase());
            retentionObject.setSenderRuc(retentionType.getRetentionType().getAgentParty().getPartyIdentification().get(0).getID().getValue());
            retentionObject.setSenderFiscalAddress(retentionType.getRetentionType().getAgentParty().getPostalAddress().getStreetName().getValue());
            retentionObject.setSenderDepProvDist(formatDepProvDist(retentionType.getRetentionType().getAgentParty().getPostalAddress()));
            retentionObject.setSenderLogo(configData.getCompletePathLogo());

            retentionObject.setComentarios(retentionType.getTransaccion().getFE_Comentario());
            retentionObject.setTel(retentionType.getTransaccion().getTelefono());
            retentionObject.setTel1(retentionType.getTransaccion().getTelefono_1());
            retentionObject.setSenderMail(retentionType.getTransaccion().getEMail());
            retentionObject.setWeb(retentionType.getTransaccion().getWeb());
            retentionObject.setRegimenRET(retentionType.getTransaccion().getRET_Tasa());
            retentionObject.setReceiverSocialReason(retentionType.getRetentionType().getReceiverParty().getPartyLegalEntity().get(0).getRegistrationName().getValue().toUpperCase());
            retentionObject.setReceiverRuc(retentionType.getRetentionType().getReceiverParty().getPartyIdentification().get(0).getID().getValue());

            retentionObject.setRetentionItems(getRetentionItems(retentionType.getRetentionType().getSunatRetentionDocumentReference(), retentionType.getRetentionType().getSunatRetentionPercent().getValue()));

            retentionObject.setTotalAmountValue(retentionType.getRetentionType().getTotalInvoiceAmount().getValue().toString());
            BigDecimal montoSoles = new BigDecimal("0.00");
            BigDecimal importeTotal = null;
            BigDecimal importeTotalDOC = new BigDecimal("0.00");
            BigDecimal importeDocumento = new BigDecimal("0.00");
            for (int i = 0; i < retentionType.getTransaccion().getTransactionComprobantesDTOList().size(); i++) {
                montoSoles = montoSoles.add(retentionType.getTransaccion().getTransactionComprobantesDTOList().get(i).getCP_Importe().add(retentionType.getTransaccion().getTransactionComprobantesDTOList().get(i).getCP_ImporteTotal()));
                importeTotalDOC = importeTotalDOC.add(retentionType.getRetentionType().getSunatRetentionDocumentReference().get(i).getPayment().getPaidAmount().getValue());
                importeDocumento = importeDocumento.add(retentionType.getTransaccion().getTransactionComprobantesDTOList().get(i).getCP_ImporteTotal());
            }

            if (Boolean.parseBoolean(configData.getPdfBorrador())) {
                retentionObject.setValidezPDF("Este documento no tiene validez fiscal.");
            } else {
                retentionObject.setValidezPDF("");
            }
            retentionObject.setMontoenSoles(montoSoles.toString());
            retentionObject.setMontoTotalDoc(importeTotalDOC.toString());
            retentionObject.setTotal_doc_value(importeDocumento.toString());

            for (int i = 0; i < retentionType.getTransaccion().getTransactionPropertiesDTOList().size(); i++) {
                if (retentionType.getTransaccion().getTransactionPropertiesDTOList().get(i).getId().equalsIgnoreCase("1000")) {
                    retentionObject.setLetterAmountValue(retentionType.getTransaccion().getTransactionPropertiesDTOList().get(i).getValor());
                }
            }

            List<WrapperItemObject> listaItem = new ArrayList<WrapperItemObject>();

            for (TransactionComprobantesDTO transaccion : retentionType.getTransaccion().getTransactionComprobantesDTOList()) {
                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<>();
                List<String> newlist = new ArrayList<>();

                // Utilizar reflección para obtener los campos y valores del objeto
                for (Field field : transaccion.getClass().getDeclaredFields()) {
                    field.setAccessible(true); // Permitir acceso a campos privados

                    try {
                        Object value = field.get(transaccion);
                        if (value != null) {
                            String fieldName = field.getName();

                            // Si el nombre del campo comienza con "U_", corta los dos primeros caracteres
                            if (fieldName.startsWith("U_")) {
                                fieldName = fieldName.substring(2);
                            }

                            itemObjectHash.put(fieldName, value.toString());
                            newlist.add(value.toString());

                        }

                        // Conversiones específicas para fechas
                        if (field.getName().equals("DOC_FechaEmision") || field.getName().equals("CP_Fecha")) {
                            String formattedDate = DateConverter.convertToDate(value);
                            if (formattedDate != null) {
                                itemObjectHash.put(field.getName(), formattedDate);
                            }
                        }

                    } catch (IllegalAccessException e) {
                        logger.error("Error accediendo al campo: " + field.getName(), e);
                    }
                }

                itemObject.setLstItemHashMap(itemObjectHash);
                itemObject.setLstDinamicaItem(newlist);
                listaItem.add(itemObject);
            }

            retentionObject.setComentarios(retentionType.getTransaccion().getFE_Comentario());
            if (listaItem != null && !listaItem.isEmpty()) {
                WrapperItemObject ultimoItem = listaItem.getLast();

                if (ultimoItem != null) {
                    Map<String, String> itemMap = ultimoItem.getLstItemHashMap();

                    if (itemMap != null) {
                        String valor = itemMap.get("VALOR");

                        if ("2".equals(valor)) {
                            if (retentionObject != null && retentionObject.getComentarios() != null) {
                                itemMap.put("Descripcion", retentionObject.getComentarios());
                            } else {
                                itemMap.put("Descripcion", ""); // Valor por defecto si no hay comentario
                            }
                        }
                    }
                }
            }

            retentionObject.setItemListDynamic(listaItem);

            String barcodeValue = generateBarcodeInfoV2(retentionType.getRetentionType().getId().getValue(), IUBLConfig.DOC_RETENTION_CODE, retentionObject.getIssueDate(), retentionType.getRetentionType().getTotalInvoiceAmount().getValue(), BigDecimal.ZERO, retentionType.getRetentionType().getAgentParty(), retentionType.getRetentionType().getReceiverParty(), retentionType.getRetentionType().getUblExtensions());

            InputStream inputStream;
            InputStream inputStreamPDF;

            String rutaPath = configData.getRutaBaseDoc() + File.separator + retentionType.getTransaccion().getDocIdentidad_Nro() + File.separator + "CodigoQR" + File.separator + retentionType.getRetentionType().getId().getValue() + ".png";
            File f = new File(configData.getRutaBaseDoc() + File.separator + retentionType.getTransaccion().getDocIdentidad_Nro() + File.separator + "CodigoQR");
            if (!f.exists()) {
                f.mkdirs();
            }

            inputStream = generateQRCode(barcodeValue, rutaPath);

            retentionObject.setCodeQR(inputStream);

            f = new File(configData.getRutaBaseDoc() + File.separator + retentionType.getTransaccion().getDocIdentidad_Nro() + File.separator + "CodigoPDF417");
            rutaPath = configData.getRutaBaseDoc() + File.separator + retentionType.getTransaccion().getDocIdentidad_Nro() + File.separator + "CodigoPDF417" + File.separator + retentionType.getRetentionType().getId().getValue() + ".png";
            if (!f.exists()) {
                f.mkdirs();
            }
            inputStreamPDF = generatePDF417Code(barcodeValue, rutaPath, 200, 200, 1);

            retentionObject.setBarcodeValue(inputStreamPDF);

            String digestValue = generateDigestValue(retentionType.getRetentionType().getUblExtensions());

            if (logger.isInfoEnabled()) {
                logger.info("generateBoletaPDF() [" + this.docUUID + "] VALOR RESUMEN: \n" + digestValue);
            }

            retentionObject.setDigestValue(digestValue);

            retentionObject.setResolutionCodeValue("resolutionCde");

            perceptionBytes = createRetentionPDF(retentionObject, docUUID, configData);

        } catch (PDFReportException e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] PDFReportException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
        } catch (Exception e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            ErrorObj error = new ErrorObj(IVenturaError.ERROR_2.getId(), e.getMessage());
        }
        return perceptionBytes;

    }

    public byte[] createRetentionPDF(RetentionObject retentionObject,
                                     String docUUID, ConfigData configData) throws PDFReportException {

        Map<String, Object> parameterMap;

        byte[] pdfDocument = null;

        if (null == retentionObject) {
            throw new PDFReportException(IVenturaError.ERROR_406);
        } else {

            try {
                parameterMap = new HashMap<String, Object>();
                parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER, retentionObject.getDocumentIdentifier());
                parameterMap.put(IPDFCreatorConfig.ISSUE_DATE, retentionObject.getIssueDate());
                if (StringUtils.isNotBlank(retentionObject
                        .getSunatTransaction())) {
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_LABEL, IPDFCreatorConfig.OPERATION_TYPE_DSC);
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_VALUE, retentionObject.getSunatTransaction());
                }
                parameterMap.put(IPDFCreatorConfig.SENDER_SOCIAL_REASON, retentionObject.getSenderSocialReason());
                parameterMap.put(IPDFCreatorConfig.SENDER_RUC, retentionObject.getSenderRuc());
                parameterMap.put(IPDFCreatorConfig.SENDER_FISCAL_ADDRESS, retentionObject.getSenderFiscalAddress());
                parameterMap.put(IPDFCreatorConfig.SENDER_DEP_PROV_DIST, retentionObject.getSenderDepProvDist());
                parameterMap.put(IPDFCreatorConfig.SENDER_CONTACT, retentionObject.getSenderContact());
                parameterMap.put(IPDFCreatorConfig.SENDER_MAIL, retentionObject.getSenderMail());
                parameterMap.put(IPDFCreatorConfig.SENDER_LOGO_PATH, retentionObject.getSenderLogo());
                parameterMap.put(IPDFCreatorConfig.SENDER_MAIL, retentionObject.getSenderMail());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL, retentionObject.getTel());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL_1, retentionObject.getTel1());
                parameterMap.put(IPDFCreatorConfig.SENDER_WEB, retentionObject.getWeb());
                parameterMap.put(IPDFCreatorConfig.COMMENTS, retentionObject.getComentarios());
                parameterMap.put(IPDFCreatorConfig.TOTAL_DOCUMENTO, retentionObject.getTotal_doc_value());
                parameterMap.put(IPDFCreatorConfig.VALIDEZPDF, retentionObject.getValidezPDF());
                parameterMap.put(IPDFCreatorConfig.REGIMENRET, retentionObject.getRegimenRET());
                parameterMap.put(IPDFCreatorConfig.IMPORTETOTALDOC, retentionObject.getMontoTotalDoc());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_SOCIAL_REASON, retentionObject.getReceiverSocialReason());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_RUC, retentionObject.getReceiverRuc());
                parameterMap.put(IPDFCreatorConfig.TOTAL_AMOUNT_VALUE, retentionObject.getTotalAmountValue());
                parameterMap.put(IPDFCreatorConfig.TOTAL_AMOUNT_VALUE_SOLES, retentionObject.getMontoenSoles());

                if (configData.getImpresionPDF().equalsIgnoreCase("Codigo QR")) {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, retentionObject.getCodeQR());
                } else {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, null);
                }

                if (configData.getImpresionPDF().equalsIgnoreCase("PDF 147")) {
                    parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, retentionObject.getBarcodeValue());
                } else {
                    parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, null);
                }

                if (configData.getImpresionPDF().equalsIgnoreCase("Valor Resumen")) {
                    parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, retentionObject.getDigestValue());
                } else {
                    parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, null);
                }
                parameterMap.put(IPDFCreatorConfig.LETTER_AMOUNT_VALUE, retentionObject.getLetterAmountValue());

                String documentName = (configData.getPdfIngles() != null && configData.getPdfIngles().equals("Si")) ? "retentionDocument_Ing.jrxml" : "retentionDocument.jrxml";
                JasperReport jasperReport = jasperReportConfig.getJasperReportForRuc(retentionObject.getSenderRuc(), documentName, configData.getRutaBaseConfig());
                JasperPrint iJasperPrint = JasperFillManager.fillReport(jasperReport, parameterMap, new JRBeanCollectionDataSource(retentionObject.getItemListDynamic()));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
                pdfDocument = outputStream.toByteArray();
            } catch (Exception e) {
                throw new PDFReportException(e.getMessage());
            }
        }

        return pdfDocument;
        // createInvoicePDF
    }

    protected List<RetentionItemObject> getRetentionItems(
            List<SUNATRetentionDocumentReferenceType> retentionLines,
            String porcentaje) throws PDFReportException {

        List<RetentionItemObject> itemList = null;

        if (null != retentionLines && 0 < retentionLines.size()) {
            /* Instanciando la lista de objetos */
            itemList = new ArrayList<RetentionItemObject>(retentionLines.size());

            try {
                for (SUNATRetentionDocumentReferenceType iLine : retentionLines) {
                    RetentionItemObject retencionItemObj = new RetentionItemObject();

                    retencionItemObj.setFechaEmision(DateUtil.formatIssueDate(iLine.getIssueDate().getValue()));
                    retencionItemObj.setFechaPago(DateUtil.formatIssueDate(iLine.getPayment().getPaidDate().getValue()));
                    retencionItemObj.setTipoDocumento(iLine.getId().getSchemeID());
                    retencionItemObj.setNroDoc(iLine.getId().getValue());
                    retencionItemObj.setMontoTotal(iLine.getTotalInvoiceAmount().getValue().toString());
                    retencionItemObj.setMonedaMontoTotal(iLine.getTotalInvoiceAmount().getCurrencyID() + " ");
                    retencionItemObj.setMonedaRetencion("PEN ");
                    retencionItemObj.setValorRetencion(iLine.getSunatRetentionInformation().getSunatRetentionAmount().getValue().toString());
                    //invoiceItemObj.setMontoSoles(iLine.getPayment().getPaidAmount().getValue().multiply(iLine.getSunatRetentionInformation().getExchangeRate().getCalculationRate().getValue()).toString());
                    if (iLine.getId().getSchemeID().equalsIgnoreCase("07")) {
                        retencionItemObj.setMontoSoles(iLine.getPayment().getPaidAmount().getValue().multiply(iLine.getSunatRetentionInformation().getExchangeRate().getCalculationRate().getValue()).toString());
                    } else {
                        retencionItemObj.setMontoSoles(iLine.getSunatRetentionInformation().getSunatRetentionAmount().getValue().add(iLine.getSunatRetentionInformation().getSunatNetTotalPaid().getValue()).toString());
                    }
                    retencionItemObj.setNumeroPago(iLine.getPayment().getID().getValue());

                    if (iLine.getId().getSchemeID().equalsIgnoreCase("07")) {
                        retencionItemObj.setImporteNeto(BigDecimal.ZERO);
                    } else {
                        retencionItemObj.setImporteNeto(iLine.getSunatRetentionInformation().getSunatNetTotalPaid().getValue());
                    }
                    retencionItemObj.setTipoCambio(iLine.getSunatRetentionInformation().getExchangeRate().getCalculationRate().getValue().toString());
                    BigDecimal bigDecimal = new BigDecimal(porcentaje);
                    bigDecimal = bigDecimal.setScale(3, RoundingMode.HALF_UP);
                    retencionItemObj.setPorcentaje(bigDecimal);
                    itemList.add(retencionItemObj);
                }
            } catch (PDFReportException e) {
                logger.error("getInvoiceItems() [" + this.docUUID + "] ERROR: "
                        + e.getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("getInvoiceItems() [" + this.docUUID + "] ERROR: "
                        + IVenturaError.ERROR_415.getMessage());
                throw new PDFReportException(IVenturaError.ERROR_415);
            }
        } else {
            logger.error("getInvoiceItems() [" + this.docUUID + "] ERROR: "
                    + IVenturaError.ERROR_411.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_411);
        }

        return itemList;
    } // getInvoiceItems

    public String generateBarcodeInfoV2(String identifier,
                                        String documentType, String issueDate, BigDecimal payableAmountVal,
                                        BigDecimal taxTotalList,
                                        PartyType accSupplierParty,
                                        PartyType accCustomerParty, UBLExtensionsType ublExtensions)
            throws PDFReportException {

        String barcodeValue = null;

        try {
            /* a.) Numero de RUC del emisor electronico */
            String senderRuc = accSupplierParty.getPartyIdentification().get(0).getID().getValue();

            /* b.) Tipo de comprobante de pago electronico */
            /* Parametro de entrada del metodo */

            /* c.) Numeracion conformada por serie y numero correlativo */
            String serie = identifier.substring(0, 4);
            String correlative = Integer.valueOf(identifier.substring(5))
                    .toString();

            /* d.) Sumatoria IGV, de ser el caso */
            String igvTax = null;
            BigDecimal igvTaxBigDecimal = BigDecimal.ZERO;

            igvTax = igvTaxBigDecimal.setScale(2, RoundingMode.HALF_UP)
                    .toString();

            /* e.) Importe total de la venta, cesion en uso o servicio prestado */
            String payableAmount = payableAmountVal.toString();

            /* f.) Fecha de emision */
            /* Parametro de entrada del metodo */

            /* g,) Tipo de documento del adquiriente o usuario */
            String receiverDocType = accCustomerParty.getPartyIdentification()
                    .get(0).getID().getSchemeID();

            /* h.) Numero de documento del adquiriente */
            String receiverDocNumber = accCustomerParty.getPartyIdentification()
                    .get(0).getID().getValue();

            /* i.) Valor resumen <ds:DigestValue> */
            String digestValue = getDigestValue(ublExtensions);
            // String digestValue=null;
            /* j.) Valor de la Firma digital <ds:SignatureValue> */
            String signatureValue = getSignatureValue(ublExtensions);

            /*
             * Armando el codigo de barras
             */
            barcodeValue = MessageFormat.format(
                    IPDFCreatorConfig.BARCODE_PATTERN, senderRuc, documentType,
                    serie, correlative, igvTax, payableAmount, issueDate,
                    receiverDocType, receiverDocNumber, digestValue,
                    signatureValue);
        } catch (PDFReportException e) {
            logger.error("generateBarcodeInfo() [" + this.docUUID + "] ERROR: "
                    + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateBarcodeInfo() [" + this.docUUID + "] ERROR: "
                    + IVenturaError.ERROR_418.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_418);
        }

        return barcodeValue;
    }// generateBarcodeInfo}
}
