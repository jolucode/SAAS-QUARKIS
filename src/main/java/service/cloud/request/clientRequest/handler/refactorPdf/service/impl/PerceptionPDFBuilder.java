package service.cloud.request.clientRequest.handler.refactorPdf.service.impl;

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
import service.cloud.request.clientRequest.dto.dto.*;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.PerceptionObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.WrapperItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.item.PerceptionItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.config.JasperReportConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.service.PerceptionPDFGenerator;
import service.cloud.request.clientRequest.utils.DateUtil;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1.SUNATPerceptionDocumentReferenceType;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class PerceptionPDFBuilder extends BaseDocumentService implements PerceptionPDFGenerator {

    private static final Logger logger = Logger.getLogger(PerceptionPDFBuilder.class);

    @Autowired
    private JasperReportConfig jasperReportConfig;

    String docUUID = "asd";

    @Override
    public synchronized byte[] generatePerceptionPDF(UBLDocumentWRP perceptionType, ConfigData configData) throws PDFReportException {

        byte[] perceptionBytes = null;

        try {
            PerceptionObject perceptionObj = new PerceptionObject();

            perceptionObj.setDocumentIdentifier(perceptionType.getPerceptionType().getId().getValue());

            perceptionObj.setIssueDate(DateUtil.formatIssueDate(perceptionType.getPerceptionType().getIssueDate().getValue()));

            perceptionObj.setSenderSocialReason(perceptionType.getPerceptionType().getAgentParty().getPartyLegalEntity().get(0).getRegistrationName().getValue().toUpperCase());
            perceptionObj.setSenderRuc(perceptionType.getPerceptionType().getAgentParty().getPartyIdentification().get(0).getID().getValue());
            perceptionObj.setSenderFiscalAddress(perceptionType.getPerceptionType().getAgentParty().getPostalAddress().getStreetName().getValue());
            perceptionObj.setSenderDepProvDist(formatDepProvDist(perceptionType.getPerceptionType().getAgentParty().getPostalAddress()));
            perceptionObj.setSenderLogo(configData.getCompletePathLogo());
            perceptionObj.setTelValue(perceptionType.getTransaccion().getTelefono());
            perceptionObj.setWebValue(perceptionType.getTransaccion().getWeb());
            perceptionObj.setSenderMail(perceptionType.getTransaccion().getEMail());

            perceptionObj.setReceiverSocialReason(perceptionType.getPerceptionType().getReceiverParty().getPartyLegalEntity().get(0).getRegistrationName().getValue().toUpperCase());
            perceptionObj.setReceiverRuc(perceptionType.getPerceptionType().getReceiverParty().getPartyIdentification().get(0).getID().getValue());

            perceptionObj.setPerceptionItems(getPerceptionItems(perceptionType.getPerceptionType().getSunatPerceptionDocumentReference(), new BigDecimal(perceptionType.getPerceptionType().getSunatPerceptionPercent().getValue())));
            List<WrapperItemObject> listaItem = new ArrayList<>();

            for (int i = 0; i < perceptionType.getTransaccion().getTransactionComprobantesDTOList().size(); i++) {

                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<>();
                List<String> newlist = new ArrayList<>();

                for (TransactionComprobantesDTO comprobantesDTO : perceptionType.getTransaccion().getTransactionComprobantesDTOList()) {
                    // Obtener todas las variables del objeto TransactionComprobantesDTO
                    Field[] fields = comprobantesDTO.getClass().getDeclaredFields();

                    // Iterar sobre cada campo
                    for (Field field : fields) {
                        field.setAccessible(true); // Asegurarse de que el campo es accesible

                        try {
                            // Obtener el nombre del campo y su valor
                            String nombreCampo = field.getName();
                            Object valorCampo = field.get(comprobantesDTO);

                            if (valorCampo != null) {
                                // Si el campo empieza con "U_", se elimina antes de guardarlo
                                if (nombreCampo.startsWith("U_")) {
                                    nombreCampo = nombreCampo.substring(2);
                                }

                                // Añadir el nombre y el valor al HashMap, convirtiendo el valor a String
                                itemObjectHash.put(nombreCampo, valorCampo.toString());
                                newlist.add(valorCampo.toString());
                            }
                        } catch (IllegalAccessException e) {
                            logger.error("Error accediendo al campo: " + field.getName(), e);
                        }
                    }
                }

                itemObject.setLstItemHashMap(itemObjectHash);
                itemObject.setLstDinamicaItem(newlist);
                listaItem.add(itemObject);
            }


            perceptionObj.setItemListDynamic(listaItem);
            perceptionObj.setTotalAmountValue(perceptionType.getPerceptionType().getTotalInvoiceAmount().getValue().toString());

            perceptionType.getTransaccion();
            for (int i = 0; i < perceptionType.getTransaccion().getTransactionPropertiesDTOList().size(); i++) {
                if (perceptionType.getTransaccion().getTransactionPropertiesDTOList().get(i).getId().equalsIgnoreCase("1000")) {
                    perceptionObj.setLetterAmountValue(perceptionType.getTransaccion().getTransactionPropertiesDTOList().get(i).getValor());
                }

            }
            if (Boolean.parseBoolean(configData.getPdfBorrador())) {
                perceptionObj.setValidezPDF("Este documento no tiene validez fiscal.");
            } else {
                perceptionObj.setValidezPDF("");
            }

            perceptionObj.setResolutionCodeValue("resolutionCde");
            perceptionObj.setImporteTexto(perceptionType.getTransaccion().getTransactionPropertiesDTOList().get(0).getValor());

            perceptionBytes = createPerceptionPDF(perceptionObj, docUUID, configData);// PDFPerceptionCreator.getInstance(this.documentReportPath, this.legendSubReportPath).createPerceptionPDF(perceptionObj, docUUID);
        } catch (Exception e) {
            throw new PDFReportException(e.getMessage());
        }
        return perceptionBytes;
    } // generateInvoicePDF

    public byte[] createPerceptionPDF(PerceptionObject perceptionObj,
                                      String docUUID, ConfigData configData) throws PDFReportException {
        Map<String, Object> parameterMap;

        byte[] pdfDocument = null;

        if (null == perceptionObj) {
            throw new PDFReportException(IVenturaError.ERROR_406);
        } else {
            try {
                /* Crea instancia del MAP */
                parameterMap = new HashMap<String, Object>();

                parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER,
                        perceptionObj.getDocumentIdentifier());
                parameterMap.put(IPDFCreatorConfig.ISSUE_DATE,
                        perceptionObj.getIssueDate());

                if (StringUtils.isNotBlank(perceptionObj.getSunatTransaction())) {
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_LABEL,
                            IPDFCreatorConfig.OPERATION_TYPE_DSC);
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_VALUE,
                            perceptionObj.getSunatTransaction());
                }
                parameterMap.put(IPDFCreatorConfig.SENDER_SOCIAL_REASON,
                        perceptionObj.getSenderSocialReason());
                parameterMap.put(IPDFCreatorConfig.SENDER_RUC,
                        perceptionObj.getSenderRuc());
                parameterMap.put(IPDFCreatorConfig.SENDER_FISCAL_ADDRESS,
                        perceptionObj.getSenderFiscalAddress());
                parameterMap.put(IPDFCreatorConfig.SENDER_DEP_PROV_DIST,
                        perceptionObj.getSenderDepProvDist());
                parameterMap.put(IPDFCreatorConfig.SENDER_CONTACT,
                        perceptionObj.getSenderContact());
                parameterMap.put(IPDFCreatorConfig.SENDER_MAIL,
                        perceptionObj.getSenderMail());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL,
                        perceptionObj.getTelValue());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL_1,
                        perceptionObj.getTel2Value());
                parameterMap.put(IPDFCreatorConfig.SENDER_LOGO_PATH,
                        perceptionObj.getSenderLogo());
                parameterMap.put(IPDFCreatorConfig.SENDER_WEB,
                        perceptionObj.getWebValue());
                parameterMap.put(IPDFCreatorConfig.VALIDEZPDF, perceptionObj.getValidezPDF());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_SOCIAL_REASON,
                        perceptionObj.getReceiverSocialReason());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_RUC,
                        perceptionObj.getReceiverRuc());
                parameterMap.put(IPDFCreatorConfig.TOTAL_AMOUNT_VALUE,
                        perceptionObj.getTotalAmountValue());
                parameterMap.put(IPDFCreatorConfig.LETTER_AMOUNT_VALUE,
                        perceptionObj.getLetterAmountValue());

                Map<String, String> legendMap = new HashMap<String, String>();
                legendMap.put(IPDFCreatorConfig.LEGEND_DOCUMENT_TYPE,
                        IPDFCreatorConfig.LEGEND_PERCEPTION_DOCUMENT);
                legendMap.put(IPDFCreatorConfig.RESOLUTION_CODE_VALUE,
                        perceptionObj.getResolutionCodeValue());

                String documentName = (configData.getPdfIngles() != null && configData.getPdfIngles().equals("Si")) ? "perceptionDocument_Ing.jrxml" : "perceptionDocument.jrxml";
                JasperReport jasperReport = jasperReportConfig.getJasperReportForRuc(perceptionObj.getSenderRuc(), documentName, configData.getRutaBaseConfig());

                JasperPrint iJasperPrint = JasperFillManager.fillReport(
                        jasperReport,
                        parameterMap,
                        new JRBeanCollectionDataSource(perceptionObj
                                .getItemListDynamic()));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
                pdfDocument = outputStream.toByteArray();
            } catch (Exception e) {
                throw new PDFReportException(e.getMessage());
            }
        }
        return pdfDocument;
    }



    protected List<PerceptionItemObject> getPerceptionItems(List<SUNATPerceptionDocumentReferenceType> perceptionLines, BigDecimal porcentaje) throws PDFReportException {
        List<PerceptionItemObject> itemList = null;

        if (null != perceptionLines && 0 < perceptionLines.size()) {
            /* Instanciando la lista de objetos */
            itemList = new ArrayList<PerceptionItemObject>(
                    perceptionLines.size());

            try {
                for (SUNATPerceptionDocumentReferenceType iLine : perceptionLines) {
                    PerceptionItemObject invoiceItemObj = new PerceptionItemObject();

                    invoiceItemObj.setFechaEmision(DateUtil.formatIssueDate(iLine
                            .getIssueDate().getValue()));
                    invoiceItemObj
                            .setTipoDocumento(iLine.getId().getSchemeID());
                    invoiceItemObj.setNumSerieDoc(iLine.getId().getValue());
                    invoiceItemObj.setMonedaMontoTotal(iLine.getTotalInvoiceAmount().getCurrencyID() + " ");
                    invoiceItemObj.setPrecioVenta(iLine.getTotalInvoiceAmount()
                            .getValue());
                    invoiceItemObj.setMonedaPercepcion("PEN ");
                    invoiceItemObj.setImportePercepcion(iLine
                            .getSunatPerceptionInformation()
                            .getPerceptionAmount().getValue());
                    invoiceItemObj.setMontoTotalCobrado(iLine
                            .getSunatPerceptionInformation()
                            .getSunatNetTotalCashed().getValue());
                    invoiceItemObj.setPorcentajePercepcion(porcentaje);
                    itemList.add(invoiceItemObj);
                }
            } catch (PDFReportException e) {
                throw e;
            } catch (Exception e) {
                throw new PDFReportException(IVenturaError.ERROR_415);
            }
        } else {
            throw new PDFReportException(IVenturaError.ERROR_411);
        }
        return itemList;
    }

}
