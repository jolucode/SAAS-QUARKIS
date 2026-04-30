package service.cloud.request.clientRequest.handler.refactorPdf.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.encoder.PDF417;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import service.cloud.request.clientRequest.dto.dto.TransactionImpuestosDTO;
import service.cloud.request.clientRequest.dto.dto.TransactionTotalesDTO;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.legend.LegendObject;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.UBLDocumentException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.NoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionsType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class BaseDocumentService {

    protected static final Logger logger = LoggerFactory.getLogger(BaseDocumentService.class);

    protected InputStream generatePDF417Code(String qrCodeData, String filePath, int width, int height, int margin) {

        try {
            BitMatrix bitMatrixFromEncoder = null;
            PDF417 objPdf417 = new PDF417();
            objPdf417.generateBarcodeLogic(qrCodeData, 5);
            objPdf417.setEncoding(StandardCharsets.UTF_8);
            int aspectRatio = 4;
            byte[][] originalScale = objPdf417.getBarcodeMatrix().getScaledMatrix(1, aspectRatio);

            boolean rotated = false;
            if ((height > width) != (originalScale[0].length < originalScale.length)) {
                originalScale = rotateArray(originalScale);
                rotated = true;
            }

            int scaleX = width / originalScale[0].length;
            int scaleY = height / originalScale.length;

            int scale;
            if (scaleX < scaleY) {
                scale = scaleX;
            } else {
                scale = scaleY;
            }

            if (scale > 1) {
                byte[][] scaledMatrix = objPdf417.getBarcodeMatrix().getScaledMatrix(scale, scale * aspectRatio);
                if (rotated) {
                    scaledMatrix = rotateArray(scaledMatrix);
                }
                bitMatrixFromEncoder = bitMatrixFromBitArray(scaledMatrix, margin);
            }
            bitMatrixFromEncoder = bitMatrixFromBitArray(originalScale, margin);

            MatrixToImageWriter.writeToFile(bitMatrixFromEncoder, filePath.substring(filePath.lastIndexOf('.') + 1), new File(filePath));

            FileInputStream fis = new FileInputStream(filePath);
            InputStream is = fis;
            return is;

        } catch (WriterException ex) {

        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        return null;

    }

    private static BitMatrix bitMatrixFromBitArray(byte[][] input, int margin) {
        // Creates the bit matrix with extra space for whitespace
        BitMatrix output = new BitMatrix(input[0].length + 2 * margin, input.length + 2 * margin);
        output.clear();
        for (int y = 0, yOutput = output.getHeight() - margin - 1; y < input.length; y++, yOutput--) {
            byte[] inputY = input[y];
            for (int x = 0; x < input[0].length; x++) {
                // Zero is white in the byte matrix
                if (inputY[x] == 1) {
                    output.set(x + margin, yOutput);
                }
            }
        }
        return output;
    }

    protected static byte[][] rotateArray(byte[][] bitarray) {
        byte[][] temp = new byte[bitarray[0].length][bitarray.length];
        for (int ii = 0; ii < bitarray.length; ii++) {
            // This makes the direction consistent on screen when rotating the
            // screen;
            int inverseii = bitarray.length - ii - 1;
            for (int jj = 0; jj < bitarray[0].length; jj++) {
                temp[jj][inverseii] = bitarray[ii][jj];
            }
        }
        return temp;
    }

    /***/

    public static InputStream generateQRCode(String qrCodeData, String filePath) {

        try {

            String charset = "utf-8"; // or "ISO-8859-1"
            Map hintMap = new HashMap();

            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
            createQRCode(qrCodeData, filePath, charset, hintMap, 200, 200);

            FileInputStream fis = new FileInputStream(filePath);
            InputStream is = fis;
            return is;

        } catch (WriterException | IOException ex) {
            logger.error(ex.getMessage());
        }
        return null;
    }


    public static void createQRCode(String qrCodeData, String filePath, String charset, Map hintMap, int qrCodeheight, int qrCodewidth) throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset), BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
        MatrixToImageWriter.writeToFile(matrix, filePath.substring(filePath.lastIndexOf('.') + 1), new File(filePath));
    }

    protected BigDecimal getTaxTotalValueV21(List<TaxTotalType> taxTotalList) {

        for (int i = 0; i < taxTotalList.size(); i++) {
            for (int j = 0; j < taxTotalList.get(i).getTaxSubtotal().size(); j++) {
                if (taxTotalList.get(i).getTaxSubtotal().get(j).getTaxCategory().getTaxScheme().getID().getValue().equalsIgnoreCase("1000")) {
                    return taxTotalList.get(i).getTaxAmount().getValue();
                }
            }

        }
        return BigDecimal.ZERO;
    }

    protected List<LegendObject> getLegendList(
            Map<String, LegendObject> legendsMap) {
        List<LegendObject> legendList = null;
        if (null != legendsMap && 0 < legendsMap.size()) {
            legendList = new ArrayList<LegendObject>(legendsMap.values());
        } else {
            LegendObject legendObj = new LegendObject();
            legendObj.setLegendValue(IPDFCreatorConfig.LEGEND_DEFAULT_EMPTY);

            legendList = new ArrayList<LegendObject>();
            legendList.add(legendObj);
        }

        return legendList;
    } // getLegendList

    protected Map<String, LegendObject> getaddLeyends(List<NoteType> lstNote) throws PDFReportException {
        Map<String, LegendObject> legendsMap = null;
        try {
            legendsMap = new HashMap<String, LegendObject>();
            if (lstNote != null) {
                if (lstNote.size() > 0) {
                    for (int i = 0; i < lstNote.size(); i++) {
                        LegendObject legendObj = new LegendObject();
                        legendObj.setLegendValue(lstNote.get(i).getValue());
                        legendsMap.put(lstNote.get(i).getLanguageLocaleID(), legendObj);
                    }
                }
            }

        } catch (Exception ex) {
            logger.error("getAdditionalProperties() ["
                    + "] ERROR: " + IVenturaError.ERROR_420.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_420);
        }
        return legendsMap;
    }

    protected String getDigestValue(UBLExtensionsType ublExtensions)
            throws Exception {
        String digestValue = null;
        try {
            int lastIndex = ublExtensions.getUBLExtension().size() - 1;
            UBLExtensionType ublExtension = ublExtensions.getUBLExtension()
                    .get(lastIndex);

            NodeList nodeList = ublExtension.getExtensionContent().getAny()
                    .getElementsByTagName(IUBLConfig.UBL_DIGESTVALUE_TAG);
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeName()
                        .equalsIgnoreCase(IUBLConfig.UBL_DIGESTVALUE_TAG)) {
                    digestValue = nodeList.item(i).getTextContent();
                    break;
                }
            }

            if (StringUtils.isBlank(digestValue)) {
                throw new PDFReportException(IVenturaError.ERROR_423);
            }
        } catch (PDFReportException e) {
            throw e;
        } catch (Exception e) {
            logger.error("getDigestValue() Exception -->" + e.getMessage());
            throw e;
        }
        return digestValue;
    } // getDigestValue

    protected String generateDigestValue(UBLExtensionsType ublExtensions)
            throws PDFReportException {

        String digestValue = null;
        try {
            digestValue = getDigestValue(ublExtensions);
        } catch (PDFReportException e) {
            logger.error("generateBarcodeInfo() [" + "] ERROR: "
                    + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateBarcodeInfo() [" + "] ERROR: "
                    + IVenturaError.ERROR_418.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_418);
        }
        return digestValue;
    }

    protected String generateBarCodeInfoString(String RUC_emisor_electronico, String documentType, String serie, String correlativo, List<TaxTotalType> taxTotalList, String issueDate, String Importe_total_venta, String Tipo_documento_adquiriente, String Numero_documento_adquiriente, UBLExtensionsType ublExtensions) throws PDFReportException {
        String barcodeValue = "";
        try {

            String digestValue = getDigestValue(ublExtensions);
            String Sumatoria_IGV = "";
            if (taxTotalList != null) {
                Sumatoria_IGV = getTaxTotalValueV21(taxTotalList).toString();
            }
            barcodeValue = MessageFormat.format(IPDFCreatorConfig.BARCODE_PATTERN, RUC_emisor_electronico, documentType, serie, correlativo, Sumatoria_IGV, Importe_total_venta, issueDate, Tipo_documento_adquiriente, Numero_documento_adquiriente, digestValue);

        } catch (PDFReportException e) {
            logger.error("generateBarcodeInfo() [" + "] ERROR: "
                    + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateBarcodeInfo() [" + "] ERROR: "
                    + IVenturaError.ERROR_418.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_418);
        }

        return barcodeValue;
    }

    protected BigDecimal getTaxTotalValue2(List<TransactionImpuestosDTO> taxTotalList,
                                           String taxTotalCode) throws PDFReportException {
        BigDecimal taxValue = BigDecimal.ZERO;
        for (int i = 0; i < taxTotalList.size(); i++) {
            if (taxTotalList.get(i).getTipoTributo().equalsIgnoreCase(taxTotalCode)) {
                taxValue = taxValue.add(taxTotalList.get(i).getMonto());
            }
        }
        return taxValue;
    }

    protected BigDecimal getTaxTotalValue(List<TaxTotalType> taxTotalList, String taxTotalCode) {
        BigDecimal taxValue = BigDecimal.ZERO;

        for (int i = 0; i < taxTotalList.size(); i++) {
            if (taxTotalList.get(i).getTaxSubtotal().get(0).getTaxCategory().getTaxScheme().getID().getValue().equalsIgnoreCase(taxTotalCode)) {
                taxValue = taxTotalList.get(i).getTaxAmount().getValue();
            }
        }
        return taxValue;
    } // getTaxTotalValue

    protected String getCurrency(BigDecimal value, String currencyCode)
            throws Exception {
        String moneyStr = null;

        Locale locale = new Locale(IPDFCreatorConfig.LANG_PATTERN,
                IPDFCreatorConfig.LOCALE_PE);
        java.text.NumberFormat format = java.text.NumberFormat
                .getCurrencyInstance(locale);

        if (StringUtils.isNotBlank(currencyCode)) {
            Currency currency = Currency.getInstance(currencyCode);
            format.setCurrency(currency);
        }

        if (null != value) {
            Double money = value.doubleValue();

            money = money * 100;
            long tmp = Math.round(money);
            money = (double) tmp / 100;

            if (0 > money) {
                String tempDesc = format.format(money);
                java.text.DecimalFormat decF = new java.text.DecimalFormat(
                        IPDFCreatorConfig.PATTERN_FLOAT_DEC);
                moneyStr = tempDesc.substring(1, 4) + " " + decF.format(money);
            } else {
                moneyStr = format.format(money);
            }
        }

        if (null == currencyCode) {
            moneyStr = moneyStr.substring(4);
        }

        return moneyStr;
    } // getCurrency

    protected BigDecimal getTransaccionTotales(List<TransactionTotalesDTO> lstTotales, String addiMonetaryValue) {

        if (lstTotales != null) {
            if (lstTotales.size() > 0) {
                for (int i = 0; i < lstTotales.size(); i++) {
                    if (lstTotales.get(i).getId().equalsIgnoreCase(addiMonetaryValue)) {
                        return lstTotales.get(i).getMonto();
                    }
                }
            }
        }
        return BigDecimal.ZERO;
    }

    protected String getCurrencyV3(BigDecimal value, String currencyCode)
            throws Exception {
        String moneyStr = null;


        Locale locale = new Locale(IPDFCreatorConfig.LANG_PATTERN,
                IPDFCreatorConfig.LOCALE_PE);
        java.text.NumberFormat format = java.text.NumberFormat
                .getCurrencyInstance(locale);

        if (StringUtils.isNotBlank(currencyCode)) {
            Currency currency = Currency.getInstance(currencyCode);
            format.setCurrency(currency);
        }

        if (null != value) {
            Double money = value.doubleValue();

            money = money * 100;
            long tmp = Math.round(money);
            money = (double) tmp / 100;

            if (0 > money) {
                String tempDesc = format.format(money);
                java.text.DecimalFormat decF = new java.text.DecimalFormat(
                        "###,###.00", new DecimalFormatSymbols(Locale.US));
                //System.out.println(decF.format(money));
                moneyStr = tempDesc.substring(1, 4) + " " + decF.format(money);
            } else {
                moneyStr = format.format(money);
            }
        }

        if (null == currencyCode) {
            moneyStr = moneyStr.substring(4);
        }

        return moneyStr;
    } // getCurrency

    protected String getRemissionGuides(
            List<DocumentReferenceType> despatchDocumentReferences) {
        String remissionGuides = null;

        if (null != despatchDocumentReferences
                && 0 < despatchDocumentReferences.size()) {
            for (DocumentReferenceType despatchDocRef : despatchDocumentReferences) {
                if (null == remissionGuides) {
                    remissionGuides = "";
                } else {
                    remissionGuides += ", ";
                }

                remissionGuides += despatchDocRef.getID().getValue();
            }
        } else {
            remissionGuides = IPDFCreatorConfig.EMPTY_VALUE;
        }

        return remissionGuides;
    }



    protected BigDecimal getSubtotalValueFromTransaction(List<TransactionTotalesDTO> transactionTotalList, String identifier) throws UBLDocumentException {
        if (null != transactionTotalList && !transactionTotalList.isEmpty()) {
            BigDecimal subtotal = BigDecimal.ZERO;
            for (TransactionTotalesDTO transaccionTotal : transactionTotalList) {
                if (Objects.equals("1005", transaccionTotal.getId())) {
                    subtotal = transaccionTotal.getMonto();
                    break;
                }
            }
            return subtotal;
        } else {
            this.logger.error("getSubtotalValueFromTransaction() [" + identifier + "] ERROR: " + IVenturaError.ERROR_330.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_330);
        }
    }


    protected String getSunatTransactionInfo(
            List<UBLExtensionType> ublExtensionList) throws PDFReportException {
        String sunatTransactionInfo = null;
        try {
            String sunatTransCode = null;
            NodeList nodeList = ublExtensionList.get(0).getExtensionContent()
                    .getAny()
                    .getElementsByTagName(IUBLConfig.UBL_SUNAT_TRANSACTION_TAG);
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeName()
                        .equalsIgnoreCase(IUBLConfig.UBL_SUNAT_TRANSACTION_TAG)) {
                    sunatTransCode = nodeList.item(i).getTextContent();
                    break;
                }
            }

            if (StringUtils.isNotBlank(sunatTransCode)) {
                sunatTransCode = sunatTransCode.replace("\n", "").trim();

                switch (sunatTransCode) {
                    case IPDFCreatorConfig.SUNAT_TRANS_VENTA_INTERNA_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_VENTA_INTERNA_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_EXPORTACION_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_EXPORTACION_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_NO_DOMICILIADOS_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_NO_DOMICILIADOS_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_VENTA_INTERNA_ANTICIPOS_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_VENTA_INTERNA_ANTICIPOS_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_VENTA_ITINERANTE_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_VENTA_ITINERANTE_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_FACTURA_GUIA_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_FACTURA_GUIA_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_VENTA_ARROZ_PILADO_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_VENTA_ARROZ_PILADO_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_FACTURA_COMP_PERCEPCION_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_FACTURA_COMP_PERCEPCION_DSC;
                        break;
                    default:
                        throw new PDFReportException(IVenturaError.ERROR_421);
                }
            }
        } catch (PDFReportException e) {
            logger.error("getSunatTransactionInfo() ["
                    + "] ERROR: " + e.getError().getId() + "-"
                    + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getSunatTransactionInfo() ["
                    + "] Exception -->" + e.getMessage());
        }
        return sunatTransactionInfo;
    }

    protected String getDocumentReferenceValue(
            BillingReferenceType billingReference) throws PDFReportException {
        String response = null;

        if (null != billingReference) {
            String type = billingReference.getInvoiceDocumentReference()
                    .getDocumentTypeCode().getValue();
            String identifier = billingReference.getInvoiceDocumentReference()
                    .getID().getValue();

            switch (type) {
                case IUBLConfig.DOC_INVOICE_CODE:
                    response = identifier + " ("
                            + IPDFCreatorConfig.DOC_INVOICE_DESC + ")";
                    break;
                case IUBLConfig.DOC_BOLETA_CODE:
                    response = identifier + " ("
                            + IPDFCreatorConfig.DOC_BOLETA_DESC + ")";
                    break;
                case IUBLConfig.DOC_MACHINE_TICKET_CODE:
                    response = identifier + " ("
                            + IPDFCreatorConfig.DOC_MACHINE_TICKET_DESC + ")";
                    break;
                case IUBLConfig.DOC_FINANCIAL_BANKS_CODE:
                    response = identifier + " ("
                            + IPDFCreatorConfig.DOC_FINANCIAL_BANKS_DESC + ")";
                    break;
                case IUBLConfig.DOC_BANK_INSURANCE_CODE:
                    response = identifier + " ("
                            + IPDFCreatorConfig.DOC_BANK_INSURANCE_DESC + ")";
                    break;
                case IUBLConfig.DOC_ISSUED_BY_AFP_CODE:
                    response = identifier + " ("
                            + IPDFCreatorConfig.DOC_ISSUED_BY_AFP_DESC + ")";
                    break;
                default:
                    response = identifier + " ("
                            + type+ ")";
            }
        } else {
            logger.error("getDocumentReferenceValue() ["
                    + "] ERROR: " + IVenturaError.ERROR_428.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_428);
        }
        return response;
    }


    protected String getContractDocumentReference(
            List<DocumentReferenceType> contractDocumentReferences, String code) {
        String contractDocRefResponse = null;

        if (null != contractDocumentReferences
                && 0 < contractDocumentReferences.size()) {
            for (DocumentReferenceType contDocumentRef : contractDocumentReferences) {
                if (contDocumentRef.getDocumentTypeCode().getValue()
                        .equalsIgnoreCase(code)) {
                    contractDocRefResponse = contDocumentRef.getID().getValue();
                }
            }
        }

        if (StringUtils.isBlank(contractDocRefResponse)) {
            contractDocRefResponse = IPDFCreatorConfig.EMPTY_VALUE;
        }
        return contractDocRefResponse;
    } // getContractDocumentReference

    protected String getSignatureValue(UBLExtensionsType ublExtensions)
            throws Exception {
        String signatureValue = null;
        try {
            int lastIndex = ublExtensions.getUBLExtension().size() - 1;
            UBLExtensionType ublExtension = ublExtensions.getUBLExtension()
                    .get(lastIndex);

            NodeList nodeList = ublExtension.getExtensionContent().getAny()
                    .getElementsByTagName(IUBLConfig.UBL_SIGNATUREVALUE_TAG);
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeName()
                        .equalsIgnoreCase(IUBLConfig.UBL_SIGNATUREVALUE_TAG)) {
                    signatureValue = nodeList.item(i).getTextContent();
                    break;
                }
            }

            if (StringUtils.isBlank(signatureValue)) {
                throw new PDFReportException(IVenturaError.ERROR_424);
            }
        } catch (PDFReportException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
        return signatureValue;
    }

    protected String generateGuiaBarcodeInfoV2(String identifier, String documentType, String issueDate, BigDecimal payableAmountVal, BigDecimal taxTotalList, SupplierPartyType accSupplierParty, CustomerPartyType accCustomerParty, UBLExtensionsType ublExtensions) throws PDFReportException {
        String barcodeValue = null;
        try {
            /* a.) Numero de RUC del emisor electronico */
            String senderRuc = accSupplierParty.getParty().getPartyIdentification().get(0).getID().getValue();
            /* b.) Tipo de comprobante de pago electronico */
            /* Parametro de entrada del metodo */
            /* c.) Numeracion conformada por serie y numero correlativo */
            String serie = identifier.substring(0, 4);
            String correlative = Integer.valueOf(identifier.substring(5)).toString();
            /* d.) Sumatoria IGV, de ser el caso */
            String igvTax = null;
            BigDecimal igvTaxBigDecimal = BigDecimal.ZERO;
            igvTax = igvTaxBigDecimal.setScale(2, RoundingMode.HALF_UP).toString();
            /* e.) Importe total de la venta, cesion en uso o servicio prestado */
            String payableAmount = payableAmountVal.toString();
            /* f.) Fecha de emision */
            /* Parametro de entrada del metodo */
            /* g,) Tipo de documento del adquiriente o usuario */
            String receiverDocType = accCustomerParty.getParty().getPartyIdentification().get(0).getID().getSchemeID();
            /* h.) Numero de documento del adquiriente */
            String receiverDocNumber = accCustomerParty.getParty().getPartyIdentification().get(0).getID().getValue();
            /* i.) Valor resumen <ds:DigestValue> */
            String digestValue = getDigestValue(ublExtensions);
            // String digestValue=null;
            /* j.) Valor de la Firma digital <ds:SignatureValue> */
            String signatureValue = getSignatureValue(ublExtensions);
            /*
             * Armando el codigo de barras
             */
            barcodeValue = MessageFormat.format(IPDFCreatorConfig.BARCODE_PATTERN, senderRuc, documentType, serie, correlative, igvTax, payableAmount, issueDate, receiverDocType, receiverDocNumber, digestValue, signatureValue);
        } catch (PDFReportException e) {
            throw e;
        } catch (Exception e) {
            throw new PDFReportException(IVenturaError.ERROR_418);
        }
        return barcodeValue;
    }

    protected String formatDueDate(Date inputDueDate) throws Exception {
        String dueDate = null;
        try {

            SimpleDateFormat sdf = null;
            sdf = new SimpleDateFormat(IPDFCreatorConfig.PATTERN_DATE);
            dueDate = sdf.format(inputDueDate);
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
        }
        return dueDate;
    }

    protected String formatDepProvDist(AddressType postalAddress)
            throws PDFReportException {
        String depProvDist = null;
        if (null != postalAddress) {
            String department = postalAddress.getCountrySubentity().getValue();
            String province = postalAddress.getCityName().getValue();
            String district = postalAddress.getDistrict().getValue();

            depProvDist = district + " - " + province + " - " + department;

        } else {
            throw new PDFReportException(IVenturaError.ERROR_410);
        }
        return depProvDist;
    }

    protected BigDecimal getPricingReferenceValue(
            List<PriceType> alternativeConditionPrice, String priceTypeCode)
            throws PDFReportException {
        BigDecimal value = null;

        if (null != alternativeConditionPrice
                && 0 < alternativeConditionPrice.size()) {
            for (PriceType altCondPrice : alternativeConditionPrice) {
                if (altCondPrice.getPriceTypeCode().getValue()
                        .equalsIgnoreCase(priceTypeCode)) {
                    value = altCondPrice.getPriceAmount().getValue();
                }
            }
        } else {
            throw new PDFReportException(IVenturaError.ERROR_416);
        }
        return value;
    } // getPricingReferenceValue

    protected BigDecimal getDiscountItem(
            List<AllowanceChargeType> allowanceCharges) {
        BigDecimal value = BigDecimal.ZERO;

        if (null != allowanceCharges && 0 < allowanceCharges.size()) {
            /*
             * Se entiende que en el caso exista una lista, solo debe tener un
             * solo valor
             */
            value = allowanceCharges.get(0).getAmount().getValue();
        }
        return value;
    }
}
