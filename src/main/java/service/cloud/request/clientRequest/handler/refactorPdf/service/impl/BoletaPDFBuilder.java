package service.cloud.request.clientRequest.handler.refactorPdf.service.impl;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import org.w3c.dom.NodeList;
import service.cloud.request.clientRequest.dto.dto.*;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.WrapperItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.legend.BoletaObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.legend.LegendObject;
import service.cloud.request.clientRequest.handler.refactorPdf.config.JasperReportConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.service.BoletaPDFGenerator;
import service.cloud.request.clientRequest.utils.DateUtil;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.NameType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.TaxableAmountType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionType;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class BoletaPDFBuilder extends BaseDocumentService  implements BoletaPDFGenerator {

    private static final Logger logger = Logger.getLogger(BoletaPDFBuilder.class);

    @Inject
    private JasperReportConfig jasperReportConfig;

    String docUUID = "asd";


    @Override
    public byte[] generateBoletaPDF(UBLDocumentWRP boletaType, ConfigData configData, String personalizacion) throws PDFReportException {
        System.out.println("SE GENERA PDF PARA BOLETA");
        byte[] boletaInBytes = null;
        try {
            if (boletaType.getInvoiceType() == null) {
                throw new PDFReportException("Error: Se está intentando generar PDF de boleta con un documento que no es una boleta. Verifique el tipo de documento.");
            }
            BoletaObject boletaObj = new BoletaObject();

            boletaObj.setDocumentIdentifier(boletaType.getInvoiceType().getID().getValue());
            boletaObj.setIssueDate(DateUtil.formatIssueDate(boletaType.getInvoiceType().getIssueDate().getValue()));

            boletaObj.setFormSap(boletaType.getTransaccion().getFE_FormSAP());

            if (StringUtils.isNotBlank(boletaType.getInvoiceType().getDocumentCurrencyCode().getName())) {
                boletaObj.setCurrencyValue(boletaType.getInvoiceType().getDocumentCurrencyCode().getName().toUpperCase());
            } else {
                boletaObj.setCurrencyValue(boletaType.getTransaccion().getDOC_MON_Nombre().toUpperCase());
            }

            if (null != boletaType.getInvoiceType().getNote() && 0 < boletaType.getInvoiceType().getNote().size()) {
                boletaObj.setDueDate(DateUtil.formatIssueDate(boletaType.getInvoiceType().getDueDate().getValue())); //boletaObj.setDueDate(boletaType.getTransaccion().getDOC_FechaVencimiento());
            } else {
                boletaObj.setDueDate(DateUtil.formatIssueDate(boletaType.getInvoiceType().getDueDate().getValue())); //boletaObj.setDueDate(boletaType.getTransaccion().getDOC_FechaVencimiento());
            }
            List<TaxTotalType> taxTotal = boletaType.getInvoiceType().getTaxTotal();
            for (TaxTotalType taxTotalType : taxTotal) {
                List<TaxSubtotalType> taxSubtotal = taxTotalType.getTaxSubtotal();
                for (TaxSubtotalType taxSubtotalType : taxSubtotal) {
                    TaxCategoryType taxCategory = taxSubtotalType.getTaxCategory();
                    TaxableAmountType taxableAmount = taxSubtotalType.getTaxableAmount();
                    TaxSchemeType taxScheme = taxCategory.getTaxScheme();
                    NameType name = taxScheme.getName();
                    String nombreImpuesto = name.getValue();
                    if ("ICBPER".equalsIgnoreCase(nombreImpuesto)) {
                        boletaObj.setImpuestoBolsa(taxableAmount.getCurrencyID() + " " + taxableAmount.getValue().toString());
                        boletaObj.setImpuestoBolsaMoneda(taxableAmount.getCurrencyID());
                    }
                }
            }
            /* Informacion de SUNATTransaction */
            String sunatTransInfo = getSunatTransactionInfo(boletaType.getInvoiceType().getUBLExtensions().getUBLExtension());
            if (StringUtils.isNotBlank(sunatTransInfo)) {
                boletaObj.setSunatTransaction(sunatTransInfo);
            }

            boletaObj.setRemissionGuides(getRemissionGuides(boletaType.getInvoiceType().getDespatchDocumentReference()));
            boletaObj.setPaymentCondition(boletaType.getTransaccion().getDOC_CondPago());
            boletaObj.setComentarios(boletaType.getTransaccion().getFE_Comentario());

            if (null != boletaType.getTransaccion().getTransactionContractDocRefListDTOS()
                    && !boletaType.getTransaccion().getTransactionContractDocRefListDTOS().isEmpty()) {

                Map<String, String> hashedMap = new HashMap<>();
                for (Map<String, String> map : boletaType.getTransaccion().getTransactionContractDocRefListDTOS()) {
                    // Suponiendo que cada mapa tiene solo un par clave-valor
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        hashedMap.put(entry.getKey(), entry.getValue());
                    }
                }
                boletaObj.setInvoicePersonalizacion(hashedMap);
            }

            List<WrapperItemObject> listaItem = new ArrayList<WrapperItemObject>();

            for (int i = 0; i < boletaType.getTransaccion().getTransactionLineasDTOList().size(); i++) {
                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<>();
                List<String> newlist = new ArrayList<>();

                Map<String, String> camposUsuarioMap = boletaType.getTransaccion().getTransactionLineasDTOList().get(i).getTransaccionLineasCamposUsuario();
                if (camposUsuarioMap != null && !camposUsuarioMap.isEmpty()) {
                    for (Map.Entry<String, String> entry : camposUsuarioMap.entrySet()) {
                        String nombreCampo = entry.getKey();
                        String valorCampo = entry.getValue();

                        if (nombreCampo.startsWith("U_")) {
                            nombreCampo = nombreCampo.substring(2);
                        }

                        itemObjectHash.put(nombreCampo, valorCampo);
                        newlist.add(valorCampo);

                    }
                }

                itemObject.setLstItemHashMap(itemObjectHash);
                itemObject.setLstDinamicaItem(newlist);
                listaItem.add(itemObject);
            }

            if (listaItem != null && !listaItem.isEmpty()) {
                WrapperItemObject ultimoItem = listaItem.getLast();

                if (ultimoItem != null) {
                    Map<String, String> itemMap = ultimoItem.getLstItemHashMap();

                    if (itemMap != null) {
                        String valor = itemMap.get("VALOR");

                        if ("2".equals(valor)) {
                            if (boletaObj != null && boletaObj.getComentarios() != null) {
                                itemMap.put("Descripcion", boletaObj.getComentarios());
                            } else {
                                itemMap.put("Descripcion", ""); // Valor por defecto si no hay comentario
                            }
                        }
                    }
                }
            }

            boletaObj.setItemsDynamic(listaItem);
            String currencyCode = boletaType.getInvoiceType().getDocumentCurrencyCode().getValue();
            BigDecimal percepctionAmount = null;
            BigDecimal perceptionPercentage = null;
            for (int i = 0; i < boletaType.getTransaccion().getTransactionTotalesDTOList().size(); i++) {
                if (boletaType.getTransaccion().getTransactionTotalesDTOList().get(i).getId().equalsIgnoreCase("2001")) {
                    percepctionAmount = boletaType.getTransaccion().getTransactionTotalesDTOList().get(i).getMonto();
                    perceptionPercentage = boletaType.getTransaccion().getTransactionTotalesDTOList().get(i).getPrcnt();
                    boletaObj.setPerceptionAmount(currencyCode + " " + boletaType.getTransaccion().getTransactionTotalesDTOList().get(i).getMonto().toString());
                    boletaObj.setPerceptionPercentage(boletaType.getTransaccion().getTransactionTotalesDTOList().get(i).getPrcnt().toString() + "%");
                }
            }
            BigDecimal retentionpercentage = null;
            for (int i = 0; i < boletaType.getTransaccion().getTransactionLineasDTOList().size(); i++) {
                for (int j = 0; j < boletaType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().size(); j++) {
                    if (boletaType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getTipoTributo().equalsIgnoreCase("2000")) {
                        boletaObj.setPorcentajeISC(boletaType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getPorcentaje().setScale(1, RoundingMode.HALF_UP).toString());
                        retentionpercentage = boletaType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getPorcentaje();
                        break;
                    }
                }
            }

            if (retentionpercentage == null) {
                boletaObj.setPorcentajeISC(BigDecimal.ZERO.toString());
            }

            if (percepctionAmount == null) {
                boletaObj.setPerceptionAmount(boletaType.getInvoiceType().getDocumentCurrencyCode().getValue() + " 0.00");
            }
            if (perceptionPercentage == null) {
                boletaObj.setPerceptionPercentage(BigDecimal.ZERO.toString());
            }

            BigDecimal prepaidAmount = null;

            if (null != boletaType.getInvoiceType().getPrepaidPayment() && !boletaType.getInvoiceType().getPrepaidPayment().isEmpty() && null != boletaType.getInvoiceType().getPrepaidPayment().get(0).getPaidAmount()) {
                prepaidAmount = boletaType.getInvoiceType().getLegalMonetaryTotal().getPrepaidAmount().getValue().negate();
                boletaObj.setPrepaidAmountValue(getCurrencyV3(prepaidAmount, currencyCode));
            } else {
                boletaObj.setPrepaidAmountValue(currencyCode + " 0.00");
                prepaidAmount = BigDecimal.ZERO;
            }

            boletaObj.setSenderSocialReason(boletaType.getTransaccion().getRazonSocial());
            boletaObj.setSenderRuc(boletaType.getTransaccion().getDocIdentidad_Nro());
            boletaObj.setSenderFiscalAddress(boletaType.getTransaccion().getDIR_NomCalle());
            boletaObj.setSenderDepProvDist(boletaType.getTransaccion().getDIR_Distrito() + " " + boletaType.getTransaccion().getDIR_Provincia() + " " + boletaType.getTransaccion().getDIR_Departamento());
            boletaObj.setSenderContact(boletaType.getTransaccion().getPersonContacto());
            boletaObj.setSenderMail(boletaType.getTransaccion().getEMail());
            boletaObj.setSenderLogo(configData.getCompletePathLogo());
            boletaObj.setTelefono(boletaType.getTransaccion().getTelefono());
            boletaObj.setTelefono_1(boletaType.getTransaccion().getTelefono_1());
            boletaObj.setWeb(boletaType.getTransaccion().getWeb());
            boletaObj.setWeb(boletaType.getTransaccion().getWeb());
            boletaObj.setPorcentajeIGV(boletaType.getTransaccion().getDOC_PorcImpuesto());
            boletaObj.setComentarios(boletaType.getTransaccion().getFE_Comentario());

            if (Boolean.parseBoolean(configData.getPdfBorrador())) {
                boletaObj.setValidezPDF("Este documento no tiene validez fiscal.");
            } else {
                boletaObj.setValidezPDF("");
            }
            List<TransactionActicipoDTO> anticipoList = boletaType.getTransaccion().getTransactionActicipoDTOList();
            String anticipos = anticipoList.parallelStream().map(TransactionActicipoDTO::getAntiDOC_Serie_Correlativo).collect(Collectors.joining(" "));
            boletaObj.setAnticipos(anticipos);

            boletaObj.setReceiverFullname(boletaType.getTransaccion().getSN_RazonSocial());
            boletaObj.setReceiverIdentifier(boletaType.getTransaccion().getSN_DocIdentidad_Nro());
            boletaObj.setReceiverIdentifierType(boletaType.getTransaccion().getSN_DocIdentidad_Tipo());
            if (boletaType.getTransaccion().getSN_DIR_NomCalle() != null && boletaType.getTransaccion().getSN_DIR_Distrito() != null && boletaType.getTransaccion().getSN_DIR_Provincia() != null && boletaType.getTransaccion().getSN_DIR_Departamento() != null)
                boletaObj.setReceiverFiscalAddress(boletaType.getTransaccion().getSN_DIR_NomCalle().toUpperCase() + " - " + boletaType.getTransaccion().getSN_DIR_Distrito().toUpperCase() + " - " + boletaType.getTransaccion().getSN_DIR_Provincia().toUpperCase() + " - " + boletaType.getTransaccion().getSN_DIR_Departamento().toUpperCase());

            BigDecimal subtotalValue = getTransaccionTotales(boletaType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1005);
            if (null != boletaType.getInvoiceType().getPrepaidPayment() && !boletaType.getInvoiceType().getPrepaidPayment().isEmpty() && null != boletaType.getInvoiceType().getPrepaidPayment().get(0).getPaidAmount()) {
                boletaObj.setSubtotalValue(getCurrency(subtotalValue.add(prepaidAmount.multiply(BigDecimal.ONE.negate())), currencyCode));
            } else {
                boletaObj.setSubtotalValue(getCurrency(subtotalValue, currencyCode));
            }

            BigDecimal igvValue = getTaxTotalValue2(boletaType.getTransaccion().getTransactionImpuestosDTOList(), IUBLConfig.TAX_TOTAL_IGV_ID);
            boletaObj.setIgvValue(getCurrency(igvValue, currencyCode));

            BigDecimal iscValue = getTaxTotalValue(boletaType.getInvoiceType().getTaxTotal(), IUBLConfig.TAX_TOTAL_ISC_ID);
            boletaObj.setIscValue(getCurrency(iscValue, currencyCode));
            boolean exist = Optional.ofNullable(boletaType.getInvoiceType().getLegalMonetaryTotal().getLineExtensionAmount()).isPresent();
            BigDecimal lineExtensionAmount = exist ? boletaType.getInvoiceType().getLegalMonetaryTotal().getLineExtensionAmount().getValue() : BigDecimal.ZERO;
            boletaObj.setAmountValue(getCurrency(lineExtensionAmount, currencyCode));
            if (null != boletaType.getInvoiceType().getLegalMonetaryTotal().getAllowanceTotalAmount() && null != boletaType.getInvoiceType().getLegalMonetaryTotal().getAllowanceTotalAmount().getValue()) {
                boletaObj.setDiscountValue(getCurrency(boletaType.getInvoiceType().getLegalMonetaryTotal().getAllowanceTotalAmount().getValue(), currencyCode));
            } else {
                boletaObj.setDiscountValue(getCurrency(BigDecimal.ZERO, currencyCode));
            }
            BigDecimal payableAmount = boletaType.getInvoiceType().getLegalMonetaryTotal().getPayableAmount().getValue();
            boletaObj.setTotalAmountValue(getCurrency(payableAmount, currencyCode));

            BigDecimal gravadaAmount = getTransaccionTotales(boletaType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1001);
            boletaObj.setGravadaAmountValue(getCurrency(gravadaAmount, currencyCode));

            BigDecimal inafectaAmount = getTransaccionTotales(boletaType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1002);
            boletaObj.setInafectaAmountValue(getCurrency(inafectaAmount, currencyCode));

            BigDecimal exoneradaAmount = getTransaccionTotales(boletaType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1003);
            boletaObj.setExoneradaAmountValue(getCurrency(exoneradaAmount, currencyCode));

            BigDecimal gratuitaAmount = getTransaccionTotales(boletaType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1004);
            if (!gratuitaAmount.equals(BigDecimal.ZERO)) {
                boletaObj.setGratuitaAmountValue(getCurrency(gratuitaAmount, currencyCode));
            } else {
                boletaObj.setGratuitaAmountValue(getCurrency(BigDecimal.ZERO, currencyCode));
            }
            String barcodeValue = generateBarCodeInfoString(boletaType.getTransaccion().getDocIdentidad_Nro(), boletaType.getTransaccion().getDOC_Codigo(), boletaType.getTransaccion().getDOC_Serie(), boletaType.getTransaccion().getDOC_Numero(), boletaType.getInvoiceType().getTaxTotal(), boletaObj.getIssueDate(), boletaType.getTransaccion().getDOC_MontoTotal().toString(), boletaType.getTransaccion().getSN_DocIdentidad_Tipo(), boletaType.getTransaccion().getSN_DocIdentidad_Nro(), boletaType.getInvoiceType().getUBLExtensions());
            String rutaPath = configData.getRutaBaseDoc() + File.separator + boletaType.getTransaccion().getDocIdentidad_Nro() + File.separator + "CodigoQR" + File.separator + "03" + File.separator + boletaType.getInvoiceType().getID().getValue() + ".png";
            File f = new File(configData.getRutaBaseDoc() + File.separator + boletaType.getTransaccion().getDocIdentidad_Nro() + File.separator + "CodigoQR" + File.separator + "03");
            if (!f.exists()) {
                f.mkdirs();
            }

            InputStream inputStream = generateQRCode(barcodeValue, rutaPath);
            boletaObj.setCodeQR(inputStream);

            f = new File(configData.getRutaBaseDoc() + File.separator + boletaType.getTransaccion().getDocIdentidad_Nro() + File.separator + "CodigoPDF417" + File.separator + "03");
            rutaPath = configData.getRutaBaseDoc() + File.separator + boletaType.getTransaccion().getDocIdentidad_Nro() + File.separator + "CodigoPDF417" + File.separator + "03" + File.separator + boletaType.getInvoiceType().getID().getValue() + ".png";
            if (!f.exists()) {
                f.mkdirs();
            }
            InputStream inputStreamPDF = generatePDF417Code(barcodeValue, rutaPath, 200, 200, 1);

            boletaObj.setBarcodeValue(inputStreamPDF);
            String digestValue = generateDigestValue(boletaType.getInvoiceType().getUBLExtensions());

            boletaObj.setDigestValue(digestValue);
            Map<String, LegendObject> legendsMap = null;

            legendsMap = getaddLeyends(boletaType.getInvoiceType().getNote());
            LegendObject legendLetters = legendsMap.get(IUBLConfig.ADDITIONAL_PROPERTY_1000);
            if(!legendsMap.isEmpty()) {
                boletaObj.setLetterAmountValue(legendLetters.getLegendValue());
            }
            legendsMap.remove(IUBLConfig.ADDITIONAL_PROPERTY_1000);
            boletaObj.setLegends(getLegendList(legendsMap));
            boletaObj.setResolutionCodeValue("resolutionCde");
            boletaInBytes = createBoletaPDF(boletaObj, configData, personalizacion);
        } catch (Exception e) {
            throw new PDFReportException(e.getMessage());
        }
        return boletaInBytes;
    } // generateBoletaPDF

    private byte[] createBoletaPDF(BoletaObject boletaObj, ConfigData configData, String personalizacion) throws PDFReportException {

        Map<String, Object> parameterMap;
        byte[] pdfDocument = null;

        if (null == boletaObj) {
            throw new PDFReportException(IVenturaError.ERROR_407);
        } else {
            try {
                parameterMap = new HashMap<String, Object>();
                parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER, boletaObj.getDocumentIdentifier());
                parameterMap.put(IPDFCreatorConfig.ISSUE_DATE, boletaObj.getIssueDate());
                parameterMap.put(IPDFCreatorConfig.DUE_DATE, boletaObj.getDueDate());
                parameterMap.put(IPDFCreatorConfig.CURRENCY_VALUE, boletaObj.getCurrencyValue());
                parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_VALUE, boletaObj.getFormSap());
                parameterMap.put(IPDFCreatorConfig.PAYMENT_CONDITION, boletaObj.getPaymentCondition());
                parameterMap.put(IPDFCreatorConfig.SELL_ORDER, boletaObj.getSellOrder());
                parameterMap.put(IPDFCreatorConfig.SELLER_NAME, boletaObj.getSellerName());
                parameterMap.put(IPDFCreatorConfig.REMISSION_GUIDE, boletaObj.getRemissionGuides());
                parameterMap.put(IPDFCreatorConfig.SENDER_SOCIAL_REASON, boletaObj.getSenderSocialReason());
                parameterMap.put(IPDFCreatorConfig.SENDER_RUC, boletaObj.getSenderRuc());
                parameterMap.put(IPDFCreatorConfig.SENDER_FISCAL_ADDRESS, boletaObj.getSenderFiscalAddress());
                parameterMap.put(IPDFCreatorConfig.SENDER_DEP_PROV_DIST, boletaObj.getSenderDepProvDist());
                parameterMap.put(IPDFCreatorConfig.SENDER_CONTACT, boletaObj.getSenderContact());
                parameterMap.put(IPDFCreatorConfig.SENDER_MAIL, boletaObj.getSenderMail());
                parameterMap.put(IPDFCreatorConfig.SENDER_LOGO_PATH, boletaObj.getSenderLogo());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL, boletaObj.getTelefono());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL_1, boletaObj.getTelefono_1());
                parameterMap.put(IPDFCreatorConfig.SENDER_WEB, boletaObj.getWeb());
                parameterMap.put(IPDFCreatorConfig.ANTICIPO_APLICADO, boletaObj.getAnticipos());
                parameterMap.put(IPDFCreatorConfig.COMMENTS, boletaObj.getComentarios());
                parameterMap.put(IPDFCreatorConfig.PORCIGV, boletaObj.getPorcentajeIGV());
                parameterMap.put(IPDFCreatorConfig.VALIDEZPDF, boletaObj.getValidezPDF());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_FULLNAME, boletaObj.getReceiverFullname());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_IDENTIFIER, boletaObj.getReceiverIdentifier());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_IDENTIFIER_TYPE, boletaObj.getReceiverIdentifierType());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_FISCAL_ADDRESS, boletaObj.getReceiverFiscalAddress());
                parameterMap.put(IPDFCreatorConfig.PERCENTAGE_PERCEPTION, boletaObj.getPerceptionPercentage());
                parameterMap.put(IPDFCreatorConfig.AMOUNT_PERCEPTION, boletaObj.getPerceptionAmount());
                parameterMap.put(IPDFCreatorConfig.PREPAID_AMOUNT_VALUE, boletaObj.getPrepaidAmountValue());
                parameterMap.put(IPDFCreatorConfig.SUBTOTAL_VALUE, boletaObj.getSubtotalValue());
                parameterMap.put(IPDFCreatorConfig.IGV_VALUE, boletaObj.getIgvValue());
                parameterMap.put(IPDFCreatorConfig.ISC_VALUE, boletaObj.getIscValue());
                parameterMap.put(IPDFCreatorConfig.AMOUNT_VALUE, boletaObj.getAmountValue());
                parameterMap.put(IPDFCreatorConfig.DISCOUNT_VALUE, boletaObj.getDiscountValue());
                parameterMap.put(IPDFCreatorConfig.TOTAL_AMOUNT_VALUE, boletaObj.getTotalAmountValue());
                parameterMap.put(IPDFCreatorConfig.GRAVADA_AMOUNT_VALUE, boletaObj.getGravadaAmountValue());
                parameterMap.put(IPDFCreatorConfig.EXONERADA_AMOUNT_VALUE, boletaObj.getExoneradaAmountValue());
                parameterMap.put(IPDFCreatorConfig.INAFECTA_AMOUNT_VALUE, boletaObj.getInafectaAmountValue());
                parameterMap.put(IPDFCreatorConfig.CAMPOS_USUARIO_CAB, boletaObj.getInvoicePersonalizacion());
                parameterMap.put(IPDFCreatorConfig.IMPUESTO_BOLSA, boletaObj.getImpuestoBolsa());
                parameterMap.put(IPDFCreatorConfig.IMPUESTO_BOLSA_MONEDA, boletaObj.getImpuestoBolsaMoneda());
                parameterMap.put(IPDFCreatorConfig.PORCISC, boletaObj.getPorcentajeISC());

                if (StringUtils.isNotBlank(boletaObj.getGratuitaAmountValue())) {
                    parameterMap.put(IPDFCreatorConfig.GRATUITA_AMOUNT_LABEL, IPDFCreatorConfig.GRATUITA_AMOUNT_LABEL_DSC);
                    parameterMap.put(IPDFCreatorConfig.GRATUITA_AMOUNT_VALUE, boletaObj.getGratuitaAmountValue());
                }

                if (configData.getImpresionPDF().equalsIgnoreCase("Codigo QR")) {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, boletaObj.getCodeQR());
                } else {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, null);
                }

                if (configData.getImpresionPDF().equalsIgnoreCase("PDF 417")) {
                    parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, boletaObj.getBarcodeValue());
                } else {
                    parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, null);
                }

                if (configData.getImpresionPDF().equalsIgnoreCase("Valor Resumen")) {
                    parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, boletaObj.getDigestValue());
                } else {
                    parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, null);
                }

                parameterMap.put(IPDFCreatorConfig.LETTER_AMOUNT_VALUE, boletaObj.getLetterAmountValue());
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DIR, configData.getRutaBaseConfig() + boletaObj.getSenderRuc() + "\\formatos\\legendReport.jasper");
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DATASOURCE, new JRBeanCollectionDataSource(boletaObj.getLegends()));
                Map<String, String> legendMap = new HashMap<String, String>();
                legendMap.put(IPDFCreatorConfig.LEGEND_DOCUMENT_TYPE, IPDFCreatorConfig.LEGEND_BOLETA_DOCUMENT);
                legendMap.put(IPDFCreatorConfig.RESOLUTION_CODE_VALUE, boletaObj.getResolutionCodeValue());
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_MAP, legendMap);

                String documentName;
                if (!personalizacion.isEmpty()) {
                    documentName = "boletaDocument_" + personalizacion + ".jrxml";
                }else {
                    documentName = (configData.getPdfIngles() != null && configData.getPdfIngles().equals("Si")) ? "boletaDocument_Ing.jrxml" : "boletaDocument.jrxml";
                }

                //String documentName = (configData.getPdfIngles() != null && configData.getPdfIngles().equals("Si")) ? "boletaDocument_Ing.jrxml" : "boletaDocument.jrxml";

                JasperReport jasperReport = jasperReportConfig.getJasperReportForRuc(boletaObj.getSenderRuc(), documentName, configData.getRutaBaseConfig());
                JasperPrint iJasperPrint = JasperFillManager.fillReport(jasperReport, parameterMap,
                        new JRBeanCollectionDataSource(boletaObj.getItemsDynamic()));

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
                pdfDocument = outputStream.toByteArray();
            } catch (Exception e) {
                throw new PDFReportException(e.getMessage());
            }
        }

        return pdfDocument;
    }

}
