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
import service.cloud.request.clientRequest.handler.refactorPdf.dto.InvoiceObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.WrapperItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.item.InvoiceItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.legend.LegendObject;
import service.cloud.request.clientRequest.handler.refactorPdf.config.JasperReportConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.service.InvoicePDFGenerator;
import service.cloud.request.clientRequest.utils.DateUtil;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.LineExtensionAmountType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.NameType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.NoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.TaxableAmountType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionsType;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class InvoicePDFBuilder extends BaseDocumentService implements InvoicePDFGenerator {

    private static final Logger logger = Logger.getLogger(InvoicePDFBuilder.class);

    @Autowired
    private JasperReportConfig jasperReportConfig;

    String docUUID = "asd";


    @Override
    public byte[] generateInvoicePDF(UBLDocumentWRP invoiceType, ConfigData configuracion, String personalizacion) throws PDFReportException {
        System.out.println("SE GENERA PDF PARA FACTURA");

        byte[] invoiceInBytes = null;
        try {
            if (invoiceType.getInvoiceType() == null) {
                throw new PDFReportException("Error: Se está intentando generar PDF de factura con un documento que no es una factura. Verifique el tipo de documento.");
            }
            InvoiceObject invoiceObj = new InvoiceObject();
            invoiceObj.setDocumentIdentifier(invoiceType.getInvoiceType().getID().getValue());
            invoiceObj.setIssueDate(DateUtil.formatIssueDate(invoiceType.getInvoiceType().getIssueDate().getValue()));
            List<TaxTotalType> taxTotal = invoiceType.getInvoiceType().getTaxTotal();
            for (TaxTotalType taxTotalType : taxTotal) {
                List<TaxSubtotalType> taxSubtotal = taxTotalType.getTaxSubtotal();
                for (TaxSubtotalType taxSubtotalType : taxSubtotal) {
                    TaxCategoryType taxCategory = taxSubtotalType.getTaxCategory();
                    TaxableAmountType taxableAmount = taxSubtotalType.getTaxableAmount();
                    TaxSchemeType taxScheme = taxCategory.getTaxScheme();
                    NameType name = taxScheme.getName();
                    String nombreImpuesto = name.getValue();
                    if ("ICBPER".equalsIgnoreCase(nombreImpuesto)) {
                        invoiceObj.setImpuestoBolsa(taxableAmount.getValue().toString());
                        invoiceObj.setImpuestoBolsaMoneda(taxableAmount.getCurrencyID());
                    }
                }
            }
            invoiceObj.setFormSap(invoiceType.getTransaccion().getFE_FormSAP());
            if (StringUtils.isNotBlank(invoiceType.getInvoiceType().getDocumentCurrencyCode().getName())) {
                invoiceObj.setCurrencyValue(invoiceType.getInvoiceType().getDocumentCurrencyCode().getName().toUpperCase());
            } else {
                invoiceObj.setCurrencyValue(invoiceType.getTransaccion().getDOC_MON_Nombre().toUpperCase());
            }
            if (null != invoiceType.getInvoiceType().getNote() && 0 < invoiceType.getInvoiceType().getNote().size()) {
                invoiceObj.setDueDate(DateUtil.formatIssueDate(invoiceType.getInvoiceType().getDueDate().getValue())); //invoiceObj.setDueDate(invoiceType.getTransaccion().getDOC_FechaVencimiento());
            } else {
                invoiceObj.setDueDate(DateUtil.formatIssueDate(invoiceType.getInvoiceType().getDueDate().getValue())); //invoiceObj.setDueDate(invoiceType.getTransaccion().getDOC_FechaVencimiento());
            }

            String Anticipos = "";
            for (int i = 0; i < invoiceType.getTransaccion().getTransactionActicipoDTOList().size(); i++) {
                Anticipos.concat(invoiceType.getTransaccion().getTransactionActicipoDTOList().get(i).getAntiDOC_Serie_Correlativo() + " ");
            }
            invoiceObj.setAnticipos(Anticipos);
            invoiceObj.setRemissionGuides(getRemissionGuides(invoiceType.getInvoiceType().getDespatchDocumentReference()));
            invoiceObj.setPaymentCondition(invoiceType.getTransaccion().getDOC_CondPago());

            invoiceObj.setSenderSocialReason(invoiceType.getTransaccion().getRazonSocial());
            invoiceObj.setSenderRuc(invoiceType.getTransaccion().getDocIdentidad_Nro());
            invoiceObj.setSenderFiscalAddress(invoiceType.getTransaccion().getDIR_NomCalle());
            invoiceObj.setSenderDepProvDist(invoiceType.getTransaccion().getDIR_Distrito() + " " + invoiceType.getTransaccion().getDIR_Provincia() + " " + invoiceType.getTransaccion().getDIR_Departamento());
            invoiceObj.setSenderContact(invoiceType.getTransaccion().getPersonContacto());
            invoiceObj.setSenderMail(invoiceType.getTransaccion().getEMail());
            invoiceObj.setSenderLogo(configuracion.getCompletePathLogo());
            invoiceObj.setTelefono(invoiceType.getTransaccion().getTelefono());
            invoiceObj.setTelefono_1(invoiceType.getTransaccion().getTelefono_1());
            invoiceObj.setWeb(invoiceType.getTransaccion().getWeb());
            invoiceObj.setPorcentajeIGV(invoiceType.getTransaccion().getDOC_PorcImpuesto());
            invoiceObj.setComentarios(invoiceType.getTransaccion().getFE_Comentario());

            invoiceObj.setReceiverSocialReason(invoiceType.getTransaccion().getSN_RazonSocial());
            invoiceObj.setReceiverRuc(invoiceType.getTransaccion().getSN_DocIdentidad_Nro());

            invoiceObj.setReceiverFiscalAddress(Objects.requireNonNullElse(invoiceType.getTransaccion().getSN_DIR_NomCalle(),"-").toUpperCase() + " - "
                    + Objects.requireNonNullElse(invoiceType.getTransaccion().getSN_DIR_Distrito(),"-").toUpperCase() + " - "
                    + Objects.requireNonNullElse(invoiceType.getTransaccion().getSN_DIR_Provincia(),"-").toUpperCase() + " - "
                    + Objects.requireNonNullElse(invoiceType.getTransaccion().getSN_DIR_Departamento(),"-").toUpperCase());
            //invoiceObj.setReceiverFiscalAddress(invoiceType.getTransaccion().getSN_DIR_NomCalle().toUpperCase() + " - " + invoiceType.getTransaccion().getSN_DIR_Distrito().toUpperCase() + " - " + invoiceType.getTransaccion().getSN_DIR_Provincia().toUpperCase() + " - " + invoiceType.getTransaccion().getSN_DIR_Departamento().toUpperCase());

            invoiceObj.setInvoiceItems(getInvoiceItems(invoiceType.getInvoiceType().getInvoiceLine()));

            String currencyCode = invoiceType.getInvoiceType().getDocumentCurrencyCode().getValue();

            if (Boolean.parseBoolean(configuracion.getPdfBorrador())) {
                invoiceObj.setValidezPDF("Este documento no tiene validez fiscal.");
            } else {
                invoiceObj.setValidezPDF("");
            }

            BigDecimal percepctionAmount = null;
            BigDecimal perceptionPercentage = null;
            for (int i = 0; i < invoiceType.getTransaccion().getTransactionTotalesDTOList().size(); i++) {
                if (invoiceType.getTransaccion().getTransactionTotalesDTOList().get(i).getId().equalsIgnoreCase("2001")) {
                    percepctionAmount = invoiceType.getTransaccion().getTransactionTotalesDTOList().get(i).getMonto();
                    perceptionPercentage = invoiceType.getTransaccion().getTransactionTotalesDTOList().get(i).getPrcnt();
                    invoiceObj.setPerceptionAmount(currencyCode + " " + invoiceType.getTransaccion().getTransactionTotalesDTOList().get(i).getMonto().toString());
                    invoiceObj.setPerceptionPercentage(invoiceType.getTransaccion().getTransactionTotalesDTOList().get(i).getPrcnt().toString() + "%");
                }
            }

            BigDecimal retentionpercentage = null;

            for (int i = 0; i < invoiceType.getTransaccion().getTransactionLineasDTOList().size(); i++) {
                for (int j = 0; j < invoiceType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().size(); j++) {
                    if (invoiceType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getTipoTributo().equalsIgnoreCase("2000")) {
                        invoiceObj.setRetentionPercentage(invoiceType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getPorcentaje().setScale(1, RoundingMode.HALF_UP).toString());
                        retentionpercentage = invoiceType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getPorcentaje();
                        break;
                    }
                }
            }

            if (retentionpercentage == null) {
                invoiceObj.setRetentionPercentage(BigDecimal.ZERO.toString());
            }

            if (percepctionAmount == null) {
                invoiceObj.setPerceptionAmount(invoiceType.getInvoiceType().getDocumentCurrencyCode().getValue() + " 0.00");
            }
            if (perceptionPercentage == null) {
                invoiceObj.setPerceptionPercentage(BigDecimal.ZERO.toString());
            }

            BigDecimal prepaidAmount = null;

            /* Agregando el monto de ANTICIPO con valor NEGATIVO */
            if (null != invoiceType.getInvoiceType().getPrepaidPayment() && !invoiceType.getInvoiceType().getPrepaidPayment().isEmpty() && null != invoiceType.getInvoiceType().getPrepaidPayment().get(0).getPaidAmount()) {
                prepaidAmount = invoiceType.getInvoiceType().getLegalMonetaryTotal().getPrepaidAmount().getValue().negate();
                // invoiceType.getInvoiceType().getPrepaidPayment().get(0).getPaidAmount().getValue().negate();
                invoiceObj.setPrepaidAmountValue(getCurrencyV3(prepaidAmount, currencyCode));
            } else {
                invoiceObj.setPrepaidAmountValue(currencyCode + " 0.00");
                prepaidAmount = BigDecimal.ZERO;
            }

            List<WrapperItemObject> listaItemC = new ArrayList<>();

            List<TransactionCuotasDTO> transaccionCuotas = invoiceType.getTransaccion().getTransactionCuotasDTOList();

            // DateFormat para el formato de entrada (yyyy-MM-dd)
            DateFormat dfEntrada = new SimpleDateFormat("yyyy-MM-dd");

            // DateFormat para el formato de salida (dd/MM/yyyy)
            DateFormat dfSalida = new SimpleDateFormat("dd/MM/yyyy");

            BigDecimal montoRetencion = (invoiceType.getTransaccion().getMontoRetencion() != null ? new BigDecimal(invoiceType.getTransaccion().getMontoRetencion()) : new BigDecimal("0.0"));

            Integer totalCuotas = 0;
            BigDecimal montoPendiente = BigDecimal.ZERO;
            String metodoPago = "";
            BigDecimal m1 = BigDecimal.ZERO, m2 = BigDecimal.ZERO, m3 = BigDecimal.ZERO;
            String f1 = "", f2 = "", f3 = "";
            String c1 = "", c2 = "", c3 = "";

            for (TransactionCuotasDTO transaccionCuota : transaccionCuotas) {
                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<>();
                List<String> newlist = new ArrayList<>();

                itemObjectHash.put("Cuota", transaccionCuota.getCuota().replaceAll("[^0-9]", ""));
                newlist.add(transaccionCuota.getCuota().replaceAll("[^0-9]", ""));
                itemObjectHash.put("FechaCuota", dfSalida.format(dfEntrada.parse(transaccionCuota.getFechaCuota())));
                newlist.add(dfSalida.format(dfEntrada.parse(transaccionCuota.getFechaCuota())));
                itemObjectHash.put("MontoCuota", transaccionCuota.getMontoCuota().toString());
                newlist.add(transaccionCuota.getMontoCuota().toString());
                itemObjectHash.put("FormaPago", transaccionCuota.getFormaPago());
                newlist.add(transaccionCuota.getFormaPago());

                itemObject.setLstItemHashMap(itemObjectHash);
                itemObject.setLstDinamicaItem(newlist);
                listaItemC.add(itemObject);
                totalCuotas++;
                montoPendiente = montoPendiente.add(transaccionCuota.getMontoCuota());
                if (totalCuotas == 1) {
                    metodoPago = transaccionCuota.getFormaPago();
                    m1 = transaccionCuota.getMontoCuota();
                    f1 = dfSalida.format(dfEntrada.parse(transaccionCuota.getFechaCuota()));
                    c1 = transaccionCuota.getCuota().replaceAll("[^0-9]", "");
                }
                if (totalCuotas == 2) {
                    m2 = transaccionCuota.getMontoCuota();
                    f2 = dfSalida.format(dfEntrada.parse(transaccionCuota.getFechaCuota()));
                    c2 = transaccionCuota.getCuota().replaceAll("[^0-9]", "");
                }
                if (totalCuotas == 3) {
                    m3 = transaccionCuota.getMontoCuota();
                    f3 = dfSalida.format(dfEntrada.parse(transaccionCuota.getFechaCuota()));
                    c3 = transaccionCuota.getCuota().replaceAll("[^0-9]", "");
                }
            }
            invoiceObj.setItemListDynamicC(listaItemC);
            invoiceObj.setTotalCuotas(totalCuotas);
            invoiceObj.setMontoPendiente(montoPendiente);
            invoiceObj.setMetodoPago(metodoPago);
            invoiceObj.setM1(m1);
            invoiceObj.setM2(m2);
            invoiceObj.setM3(m3);
            invoiceObj.setF1(f1);
            invoiceObj.setF2(f2);
            invoiceObj.setF3(f3);
            invoiceObj.setC1(c1);
            invoiceObj.setC2(c2);
            invoiceObj.setC3(c3);
            invoiceObj.setPorcentajeRetencion(retentionpercentage == null ? BigDecimal.ZERO : retentionpercentage);
            invoiceObj.setMontoRetencion(montoRetencion);
            BigDecimal baseImponibleRetencion;

            if (invoiceObj.getPorcentajeRetencion() != BigDecimal.ZERO) {
                baseImponibleRetencion = montoRetencion.divide(invoiceObj.getPorcentajeRetencion().divide(new BigDecimal("100.0")));
            } else {
                baseImponibleRetencion = BigDecimal.ZERO;
            }
            invoiceObj.setBaseImponibleRetencion(baseImponibleRetencion);

            // fin Cuotas


            List<WrapperItemObject> listaItem = new ArrayList<>();

            List<TransactionLineasDTO> transaccionLineas = invoiceType.getTransaccion().getTransactionLineasDTOList();
            for (TransactionLineasDTO transaccionLinea : transaccionLineas) {

                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<>();
                List<String> newlist = new ArrayList<>();

                // Obtener el mapa de campos de usuario desde transaccionLinea
                Map<String, String> camposUsuarioMap = transaccionLinea.getTransaccionLineasCamposUsuario();
                if (camposUsuarioMap != null && !camposUsuarioMap.isEmpty()) {
                    for (Map.Entry<String, String> entry : camposUsuarioMap.entrySet()) {
                        String nombreCampo = entry.getKey();
                        String valorCampo = entry.getValue();

                        if (nombreCampo.startsWith("U_")) {
                            // Cortar "U_" del nombreCampo
                            nombreCampo = nombreCampo.substring(2);
                        }
                        itemObjectHash.put(nombreCampo, valorCampo);
                        newlist.add(valorCampo);
                    }
                }

                // Obtener el mapa de campos de contrato desde invoiceType
                List<Map<String, String>> contractDocRefs = invoiceType.getTransaccion().getTransactionContractDocRefListDTOS();
                for (Map<String, String> contractDocRefMap : contractDocRefs) {
                    for (Map.Entry<String, String> entry : contractDocRefMap.entrySet()) {
                        String nombreCampo = entry.getKey();
                        String valorCampo = entry.getValue();

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
                            if (invoiceObj != null && invoiceObj.getComentarios() != null) {
                                itemMap.put("Descripcion", invoiceObj.getComentarios());
                            } else {
                                itemMap.put("Descripcion", ""); // Valor por defecto si no hay comentario
                            }
                        }
                    }
                }
            }

            invoiceObj.setItemListDynamic(listaItem);
            BigDecimal subtotalValue = getTransaccionTotales(invoiceType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1005);
            if (null != invoiceType.getInvoiceType().getPrepaidPayment() && !invoiceType.getInvoiceType().getPrepaidPayment().isEmpty() && null != invoiceType.getInvoiceType().getPrepaidPayment().get(0).getPaidAmount()) {
                invoiceObj.setSubtotalValue(getCurrency(invoiceType.getInvoiceType().getLegalMonetaryTotal().getLineExtensionAmount().getValue(), currencyCode));
            } else {
                invoiceObj.setSubtotalValue(getCurrency(subtotalValue, currencyCode));
            }
            if (null != invoiceType.getTransaccion().getTransactionContractDocRefListDTOS() && 0 < invoiceType.getTransaccion().getTransactionContractDocRefListDTOS().size()) {
                Map<String, String> hashedMap = new HashMap<>();
                List<Map<String, String>> contractDocRefs = invoiceType.getTransaccion().getTransactionContractDocRefListDTOS();
                for (Map<String, String> contractDocRefMap : contractDocRefs) {
                    // Asumimos que cada mapa en la lista contiene solo un par clave-valor para el campo y el valor.
                    for (Map.Entry<String, String> entry : contractDocRefMap.entrySet()) {
                        String nombreCampo = entry.getKey();
                        String valorCampo = entry.getValue();

                        hashedMap.put(nombreCampo, valorCampo);
                    }
                }
                invoiceObj.setInvoicePersonalizacion(hashedMap);
            }

            BigDecimal igvValue = getTaxTotalValue2(invoiceType.getTransaccion().getTransactionImpuestosDTOList(), IUBLConfig.TAX_TOTAL_IGV_ID);
            invoiceObj.setIgvValue(getCurrency(igvValue, currencyCode));

            BigDecimal iscValue = getTaxTotalValue2(invoiceType.getTransaccion().getTransactionImpuestosDTOList(), IUBLConfig.TAX_TOTAL_ISC_ID);
            invoiceObj.setIscValue(getCurrency(iscValue, currencyCode));

            Optional<LineExtensionAmountType> optional = Optional.ofNullable(invoiceType.getInvoiceType().getLegalMonetaryTotal().getLineExtensionAmount());
            if (optional.isPresent()) {
                BigDecimal lineExtensionAmount = invoiceType.getInvoiceType().getLegalMonetaryTotal().getLineExtensionAmount().getValue();
                invoiceObj.setAmountValue(getCurrency(lineExtensionAmount, currencyCode));
            } else {
                invoiceObj.setAmountValue(getCurrency(BigDecimal.ZERO, currencyCode));
            }
            BigDecimal descuento = null;
            TransacctionDTO transaccion = invoiceType.getTransaccion();
            BigDecimal docDescuentoTotal = transaccion.getDOC_Descuento();
            if (BigDecimal.ZERO.compareTo(docDescuentoTotal) != 0) {
                descuento = docDescuentoTotal.setScale(IUBLConfig.DECIMAL_MONETARYTOTAL_PAYABLEAMOUNT, RoundingMode.HALF_UP);
                invoiceObj.setDiscountValue(getCurrencyV3(descuento, currencyCode));
            } else {
                invoiceObj.setDiscountValue(getCurrencyV3(BigDecimal.ZERO, currencyCode));
                descuento = BigDecimal.ZERO;
            }

            BigDecimal payableAmount = invoiceType.getInvoiceType().getLegalMonetaryTotal().getPayableAmount().getValue();
            invoiceObj.setTotalAmountValue(getCurrency(payableAmount, currencyCode));

            BigDecimal gravadaAmount = getTransaccionTotales(invoiceType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1001);
            invoiceObj.setGravadaAmountValue(getCurrency(gravadaAmount, currencyCode));

            BigDecimal inafectaAmount = getTransaccionTotales(invoiceType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1002);
            invoiceObj.setInafectaAmountValue(getCurrency(inafectaAmount, currencyCode));

            BigDecimal exoneradaAmount = getTransaccionTotales(invoiceType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1003);
            invoiceObj.setExoneradaAmountValue(getCurrency(exoneradaAmount, currencyCode));

            BigDecimal gratuitaAmount = getTransaccionTotales(invoiceType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1004);
            if (!gratuitaAmount.equals(BigDecimal.ZERO)) {
                invoiceObj.setGratuitaAmountValue(getCurrency(gratuitaAmount, currencyCode));
            }
            if (gratuitaAmount.equals(BigDecimal.ZERO)) {
                invoiceObj.setGratuitaAmountValue(getCurrency(BigDecimal.ZERO, currencyCode));
            }

            invoiceObj.setNuevoCalculo(getCurrency(subtotalValue.add(prepaidAmount.multiply(BigDecimal.ONE.negate()).add(prepaidAmount).subtract(descuento)), currencyCode));

            String barcodeValue = generateBarCodeInfoString(invoiceType.getTransaccion().getDocIdentidad_Nro(), invoiceType.getTransaccion().getDOC_Codigo(), invoiceType.getTransaccion().getDOC_Serie(), invoiceType.getTransaccion().getDOC_Numero(), taxTotal, invoiceObj.getIssueDate(), invoiceType.getTransaccion().getDOC_MontoTotal().toString(), invoiceType.getTransaccion().getSN_DocIdentidad_Tipo(), invoiceType.getTransaccion().getSN_DocIdentidad_Nro(), invoiceType.getInvoiceType().getUBLExtensions());

            InputStream inputStream;
            InputStream inputStreamPDF;
            String rutaPath = ".." + File.separator + "ADJUNTOS" + File.separator + "CodigoQR" + File.separator + "01" + File.separator + invoiceType.getInvoiceType().getID().getValue() + ".png";
            File f = new File(".." + File.separator + "ADJUNTOS" + File.separator + "CodigoQR" + File.separator + "01");
            if (!f.exists()) {
                f.mkdirs();
            }

            inputStream = generateQRCode(barcodeValue, rutaPath);

            invoiceObj.setCodeQR(inputStream);

            f = new File(".." + File.separator + "ADJUNTOS" + File.separator + "CodigoPDF417" + File.separator + "01");
            rutaPath = ".." + File.separator + "ADJUNTOS" + File.separator + "CodigoPDF417" + File.separator + "01" + File.separator + invoiceType.getInvoiceType().getID().getValue() + ".png";
            if (!f.exists()) {
                f.mkdirs();
            }
            inputStreamPDF = generatePDF417Code(barcodeValue, rutaPath, 200, 200, 1);

            invoiceObj.setBarcodeValue(inputStreamPDF);
            String digestValue = generateDigestValue(invoiceType.getInvoiceType().getUBLExtensions());

            if (logger.isInfoEnabled()) {
                logger.info("generateBoletaPDF() [" + this.docUUID + "] VALOR RESUMEN: \n" + digestValue);
            }

            invoiceObj.setDigestValue(digestValue);
            Map<String, LegendObject> legendsMap = null;

            legendsMap = getaddLeyends(invoiceType.getInvoiceType().getNote());

            if(!legendsMap.isEmpty()) {
                LegendObject legendLetters = legendsMap.get(IUBLConfig.ADDITIONAL_PROPERTY_1000);
                invoiceObj.setLetterAmountValue(legendLetters.getLegendValue());
                legendsMap.remove(IUBLConfig.ADDITIONAL_PROPERTY_1000);
            }

            invoiceObj.setLegends(getLegendList(legendsMap));

            invoiceObj.setResolutionCodeValue("resolutionCde");

            /*
             * Generando el PDF de la FACTURA con la informacion recopilada.
             */
            invoiceInBytes = createInvoicePDF(invoiceObj, docUUID, configuracion, personalizacion);
        } catch (Exception e) {
            throw new PDFReportException(e.getMessage());
        }
        return invoiceInBytes;
    } // generateInvoicePDF

    public byte[] createInvoicePDF(InvoiceObject invoiceObj, String docUUID, ConfigData configuracion, String personalizacion) throws PDFReportException {
        Map<String, Object> parameterMap;
        Map<String, Object> cuotasMap;
        byte[] pdfDocument = null;

        if (null == invoiceObj) {
            throw new PDFReportException(IVenturaError.ERROR_406);
        } else {
            try {
                /* Crea instancia del MAP */
                parameterMap = new HashMap<>();

                cuotasMap = new HashMap<>();

                //================================================================================================
                //================================= AGREGANDO INFORMACION AL MAP =================================
                //================================================================================================
                parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER, invoiceObj.getDocumentIdentifier());
                parameterMap.put(IPDFCreatorConfig.ISSUE_DATE, invoiceObj.getIssueDate());
                parameterMap.put(IPDFCreatorConfig.DUE_DATE, invoiceObj.getDueDate());
                parameterMap.put(IPDFCreatorConfig.CURRENCY_VALUE, invoiceObj.getCurrencyValue());
                parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_VALUE, invoiceObj.getFormSap());
                parameterMap.put(IPDFCreatorConfig.PAYMENT_CONDITION, invoiceObj.getPaymentCondition());
                parameterMap.put(IPDFCreatorConfig.REMISSION_GUIDE, invoiceObj.getRemissionGuides());
                parameterMap.put(IPDFCreatorConfig.PORCIGV, invoiceObj.getPorcentajeIGV());
                parameterMap.put(IPDFCreatorConfig.SENDER_SOCIAL_REASON, invoiceObj.getSenderSocialReason());
                parameterMap.put(IPDFCreatorConfig.SENDER_RUC, invoiceObj.getSenderRuc());
                parameterMap.put(IPDFCreatorConfig.SENDER_FISCAL_ADDRESS, invoiceObj.getSenderFiscalAddress());
                parameterMap.put(IPDFCreatorConfig.SENDER_DEP_PROV_DIST, invoiceObj.getSenderDepProvDist());
                parameterMap.put(IPDFCreatorConfig.SENDER_CONTACT, invoiceObj.getSenderContact());
                parameterMap.put(IPDFCreatorConfig.SENDER_MAIL, invoiceObj.getSenderMail());
                parameterMap.put(IPDFCreatorConfig.SENDER_LOGO_PATH, invoiceObj.getSenderLogo());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL, invoiceObj.getTelefono());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL_1, invoiceObj.getTelefono_1());
                parameterMap.put(IPDFCreatorConfig.SENDER_WEB, invoiceObj.getWeb());
                parameterMap.put(IPDFCreatorConfig.COMMENTS, invoiceObj.getComentarios());
                parameterMap.put(IPDFCreatorConfig.ANTICIPO_APLICADO, invoiceObj.getAnticipos());

                parameterMap.put(IPDFCreatorConfig.VALIDEZPDF, invoiceObj.getValidezPDF());

                parameterMap.put(IPDFCreatorConfig.RECEIVER_SOCIAL_REASON, invoiceObj.getReceiverSocialReason());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_RUC, invoiceObj.getReceiverRuc());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_FISCAL_ADDRESS, invoiceObj.getReceiverFiscalAddress());

                parameterMap.put(IPDFCreatorConfig.PERCENTAGE_PERCEPTION, invoiceObj.getPerceptionPercentage());
                parameterMap.put(IPDFCreatorConfig.AMOUNT_PERCEPTION, invoiceObj.getPerceptionAmount());
                parameterMap.put(IPDFCreatorConfig.PORCISC, invoiceObj.getRetentionPercentage());

                parameterMap.put(IPDFCreatorConfig.PREPAID_AMOUNT_VALUE, invoiceObj.getPrepaidAmountValue());
                parameterMap.put(IPDFCreatorConfig.SUBTOTAL_VALUE, invoiceObj.getSubtotalValue());
                parameterMap.put(IPDFCreatorConfig.IGV_VALUE, invoiceObj.getIgvValue());
                parameterMap.put(IPDFCreatorConfig.ISC_VALUE, invoiceObj.getIscValue());
                parameterMap.put(IPDFCreatorConfig.AMOUNT_VALUE, invoiceObj.getAmountValue());
                parameterMap.put(IPDFCreatorConfig.DISCOUNT_VALUE, invoiceObj.getDiscountValue());
                parameterMap.put(IPDFCreatorConfig.TOTAL_AMOUNT_VALUE, invoiceObj.getTotalAmountValue());
                parameterMap.put(IPDFCreatorConfig.GRAVADA_AMOUNT_VALUE, invoiceObj.getGravadaAmountValue());
                parameterMap.put(IPDFCreatorConfig.EXONERADA_AMOUNT_VALUE, invoiceObj.getExoneradaAmountValue());
                parameterMap.put(IPDFCreatorConfig.INAFECTA_AMOUNT_VALUE, invoiceObj.getInafectaAmountValue());
                parameterMap.put(IPDFCreatorConfig.NEW_TOTAL_VALUE, invoiceObj.getNuevoCalculo());
                parameterMap.put(IPDFCreatorConfig.IMPUESTO_BOLSA, invoiceObj.getImpuestoBolsa());
                parameterMap.put(IPDFCreatorConfig.IMPUESTO_BOLSA_MONEDA, invoiceObj.getImpuestoBolsaMoneda());

                if (StringUtils.isNotBlank(invoiceObj.getGratuitaAmountValue())) {
                    parameterMap.put(IPDFCreatorConfig.GRATUITA_AMOUNT_LABEL, IPDFCreatorConfig.GRATUITA_AMOUNT_LABEL_DSC);
                    parameterMap.put(IPDFCreatorConfig.GRATUITA_AMOUNT_VALUE, invoiceObj.getGratuitaAmountValue());
                }

                if (configuracion.getImpresionPDF().equalsIgnoreCase("Codigo QR")) {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, invoiceObj.getCodeQR());
                } else {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, null);
                }

                if (configuracion.getImpresionPDF().equalsIgnoreCase("PDF 417")) {
                    parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, invoiceObj.getBarcodeValue());
                } else {
                    parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, null);
                }

                if (configuracion.getImpresionPDF().equalsIgnoreCase("Valor Resumen")) {
                    parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, invoiceObj.getDigestValue());
                } else {
                    parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, null);
                }

                parameterMap.put(IPDFCreatorConfig.LETTER_AMOUNT_VALUE, invoiceObj.getLetterAmountValue());

                parameterMap.put(IPDFCreatorConfig.SUBREPORT_PAYMENTS_DIR, configuracion.getRutaBaseConfig() + invoiceObj.getSenderRuc() +"\\formatos\\InvoiceDocumentPaymentDetail.jasper"/*this.paymentDetailReportPath*/);
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_PAYMENTS_DATASOURCE, new JRBeanCollectionDataSource(new ArrayList<>(invoiceObj.getItemListDynamicC())));

                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DIR, configuracion.getRutaBaseConfig() + invoiceObj.getSenderRuc() +"\\formatos\\legendReport.jasper"/*this.legendSubReportPath*/);
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DATASOURCE, new JRBeanCollectionDataSource(new ArrayList<>(invoiceObj.getLegends())));

                Map<String, String> legendMap = new HashMap<>();
                legendMap.put(IPDFCreatorConfig.LEGEND_DOCUMENT_TYPE, IPDFCreatorConfig.LEGEND_INVOICE_DOCUMENT);
                legendMap.put(IPDFCreatorConfig.RESOLUTION_CODE_VALUE, invoiceObj.getResolutionCodeValue());
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_MAP, legendMap);


                cuotasMap.put("M1", invoiceObj.getM1());
                cuotasMap.put("M2", invoiceObj.getM2());
                cuotasMap.put("M3", invoiceObj.getM3());

                cuotasMap.put("C1", invoiceObj.getC1());
                cuotasMap.put("C2", invoiceObj.getC2());
                cuotasMap.put("C3", invoiceObj.getC3());

                cuotasMap.put("F1", invoiceObj.getF1());
                cuotasMap.put("F2", invoiceObj.getF2());
                cuotasMap.put("F3", invoiceObj.getF3());

                cuotasMap.put("totalCuotas", invoiceObj.getTotalCuotas());
                cuotasMap.put("metodoPago", invoiceObj.getMetodoPago());
                cuotasMap.put("baseImponibleRetencion", invoiceObj.getBaseImponibleRetencion());

                cuotasMap.put("porcentajeRetencion", invoiceObj.getPorcentajeRetencion());
                cuotasMap.put("montoRetencion", invoiceObj.getMontoRetencion());
                cuotasMap.put("montoPendiente", invoiceObj.getMontoPendiente());
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_CUOTAS_MAP, cuotasMap); // parametros subreporte de cuotas (se pasa como HashMap)
                parameterMap.put(IPDFCreatorConfig.CAMPOS_USUARIO_CAB, invoiceObj.getInvoicePersonalizacion());

                String documentName;
                if (!personalizacion.isEmpty()) {
                    documentName = "invoiceDocument_" + personalizacion + ".jrxml";
                }else {
                    documentName = (configuracion.getPdfIngles() != null && configuracion.getPdfIngles().equals("Si")) ? "invoiceDocument_Ing.jrxml" : "invoiceDocument.jrxml";
                }

                JasperReport jasperReport = jasperReportConfig.getJasperReportForRuc(invoiceObj.getSenderRuc(), documentName, configuracion.getRutaBaseConfig());

                JasperPrint iJasperPrint = JasperFillManager.fillReport(jasperReport, parameterMap,
                        new JRBeanCollectionDataSource(invoiceObj.getItemListDynamic()));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
                pdfDocument = outputStream.toByteArray();
            } catch (Exception e) {
                throw new PDFReportException(e.getMessage());
            }
        }
        return pdfDocument;
    }

    protected List<InvoiceItemObject> getInvoiceItems(
            List<InvoiceLineType> invoiceLines) throws PDFReportException {
        List<InvoiceItemObject> itemList = null;

        if (null != invoiceLines && 0 < invoiceLines.size()) {
            /* Instanciando la lista de objetos */
            itemList = new ArrayList<InvoiceItemObject>(invoiceLines.size());

            try {
                for (InvoiceLineType iLine : invoiceLines) {
                    InvoiceItemObject invoiceItemObj = new InvoiceItemObject();

                    invoiceItemObj.setQuantityItem(iLine.getInvoicedQuantity()
                            .getValue());
                    // invoiceItemObj.setQuantityItem(iLine.getInvoicedQuantity().getValue().setScale(IPDFCreatorConfig.DECIMAL_ITEM_QUANTITY,
                    // RoundingMode.HALF_UP).toString());
                    String unidadMedida = Optional.ofNullable(iLine.getNote())
                            .filter(list -> !list.isEmpty())
                            .map(list -> list.get(0))
                            .map(NoteType::getValue)
                            .orElse("");
                    invoiceItemObj
                            .setUnitMeasureItem(unidadMedida);
                    invoiceItemObj.setDescriptionItem(iLine.getItem()
                            .getDescription().get(0).getValue().toUpperCase());
                    invoiceItemObj.setUnitValueItem(iLine.getPrice()
                            .getPriceAmount().getValue());
                    // invoiceItemObj.setUnitValueItem(getCurrencyV2(iLine.getPrice().getPriceAmount().getValue(),
                    // null, IPDFCreatorConfig.PATTERN_FLOAT_DEC_3));

                    BigDecimal unitPrice = getPricingReferenceValue(iLine
                                    .getPricingReference()
                                    .getAlternativeConditionPrice(),
                            IPDFCreatorConfig.ALTERNATIVE_COND_UNIT_PRICE);
                    invoiceItemObj.setUnitPriceItem(unitPrice);
                    // invoiceItemObj.setUnitPriceItem(getCurrencyV2(unitPrice,
                    // null, IPDFCreatorConfig.PATTERN_FLOAT_DEC_3));

                    invoiceItemObj.setDiscountItem(getCurrency(
                            getDiscountItem(iLine.getAllowanceCharge()), null));
                    invoiceItemObj.setAmountItem(getCurrency(iLine
                            .getLineExtensionAmount().getValue(), null));

                    itemList.add(invoiceItemObj);
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

}
