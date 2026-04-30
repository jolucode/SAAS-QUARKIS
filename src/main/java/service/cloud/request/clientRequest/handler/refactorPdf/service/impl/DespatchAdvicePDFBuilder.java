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
import service.cloud.request.clientRequest.dto.dto.TransactionImpuestosDTO;
import service.cloud.request.clientRequest.dto.dto.TransactionTotalesDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.DespatchAdviceObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.WrapperItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.item.InvoiceItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.item.RetentionItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.legend.LegendObject;
import service.cloud.request.clientRequest.handler.refactorPdf.config.JasperReportConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.service.DespatchAdvicePDFGenerator;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.NoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionsType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1.SUNATRetentionDocumentReferenceType;

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
public class DespatchAdvicePDFBuilder extends BaseDocumentService implements DespatchAdvicePDFGenerator {

    private static final Logger logger = Logger.getLogger(DespatchAdvicePDFBuilder.class);

    @Autowired
    private JasperReportConfig jasperReportConfig;

    String docUUID = "asd";

    @Override
    public synchronized byte[] generateDespatchAdvicePDF(UBLDocumentWRP despatchAdvice, ConfigData configData, String personalizacion) throws PDFReportException {

        byte[] despatchInBytes = null;

        try {
            DespatchAdviceObject despatchAdviceObject = new DespatchAdviceObject();

            despatchAdviceObject.setCodigoEmbarque(despatchAdvice.getTransaccion().getTransactionGuias().getCodigoPuerto());
            despatchAdviceObject.setCodigoMotivoTraslado(despatchAdvice.getTransaccion().getTransactionGuias().getCodigoMotivo());
            despatchAdviceObject.setDescripcionMotivoTraslado(despatchAdvice.getTransaccion().getTransactionGuias().getDescripcionMotivo());
            despatchAdviceObject.setDireccionDestino(despatchAdvice.getTransaccion().getSN_DIR_Direccion());
            despatchAdviceObject.setDireccionPartida(despatchAdvice.getTransaccion().getTransactionGuias().getDireccionPartida());
            despatchAdviceObject.setDocumentoConductor(despatchAdvice.getTransaccion().getTransactionGuias().getDocumentoConductor());
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            String fechaEmision = format.format(despatchAdvice.getTransaccion().getDOC_FechaEmision());
            despatchAdviceObject.setFechaEmision(fechaEmision);


            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");

            String fechaInicioTrasladoStr = Objects.requireNonNullElse(despatchAdvice.getTransaccion()
                    .getTransactionGuias()
                    .getFechaInicioTraslado(),"");

            if (!fechaInicioTrasladoStr.isEmpty()){
                // Parsear el String al tipo Date
                Date fechaInicioTrasladoDate = inputFormat.parse(fechaInicioTrasladoStr);
                // Formatear al formato que deseas
                String fechaInicioTraslado = outputFormat.format(fechaInicioTrasladoDate);
                // Guardar en tu objeto
                despatchAdviceObject.setFechaTraslado(fechaInicioTraslado);
            }

            despatchAdviceObject.setModalidadTraslado(despatchAdvice.getTransaccion().getTransactionGuias().getModalidadTraslado());
            despatchAdviceObject.setNombreConsumidor(despatchAdvice.getTransaccion().getSN_RazonSocial());
            despatchAdviceObject.setNombreEmisor(despatchAdvice.getTransaccion().getRazonSocial());
            despatchAdviceObject.setNumeroBultos(despatchAdvice.getTransaccion().getTransactionGuias().getNumeroBultos());
            despatchAdviceObject.setNumeroContenedor(despatchAdvice.getTransaccion().getTransactionGuias().getNumeroContenedor());
            despatchAdviceObject.setNumeroGuia(despatchAdvice.getTransaccion().getDOC_Serie() + "-" + despatchAdvice.getTransaccion().getDOC_Numero());
            despatchAdviceObject.setObervaciones(despatchAdvice.getTransaccion().getObservacione());
            despatchAdviceObject.setPesoBruto(despatchAdvice.getTransaccion().getTransactionGuias().getPeso());
            despatchAdviceObject.setTipoDocumentoConductor(despatchAdvice.getTransaccion().getTransactionGuias().getTipoDocConductor());
            despatchAdviceObject.setTipoDocumentoTransportista(despatchAdvice.getTransaccion().getTransactionGuias().getTipoDOCTransportista());
            despatchAdviceObject.setUMPesoBruto(despatchAdvice.getTransaccion().getTransactionGuias().getUnidadMedida());
            despatchAdviceObject.setNumeroDocConsumidor(despatchAdvice.getTransaccion().getSN_DocIdentidad_Nro());
            despatchAdviceObject.setNumeroDocEmisor(despatchAdvice.getTransaccion().getDocIdentidad_Nro());

            /** Harol 29-03-2024 Guia Transportista 31*/
            despatchAdviceObject.setPlacaVehiculo(despatchAdvice.getTransaccion().getTransactionGuias().getPlacaVehiculo());
            despatchAdviceObject.setLicenciaConducir(despatchAdvice.getTransaccion().getTransactionGuias().getLicenciaConductor());
            despatchAdviceObject.setRUCTransportista(despatchAdvice.getTransaccion().getTransactionGuias().getRUCTransporista());
            despatchAdviceObject.setNombreTransportista(despatchAdvice.getTransaccion().getTransactionGuias().getNombreRazonTransportista());

            /** Harol 29-03-2024 Guia Transportista 31*/
            if (despatchAdvice.getTransaccion().getDOC_Codigo().equals("31")) {
                despatchAdviceObject.setPlacaVehiculo(despatchAdvice.getTransaccion().getTransactionGuias().getPlacaVehiculo());
                despatchAdviceObject.setLicenciaConducir(despatchAdvice.getTransaccion().getTransactionGuias().getLicenciaConductor());
            }
            /** */

            List<WrapperItemObject> listaItem = new ArrayList<>();

            /** 01-03-2024 Guia de Transportista Detalle PDF*/
            if (despatchAdvice.getTransaccion().getDOC_Codigo().equals("31")) {
                String nombreDocumento = "";
                List<String> datos = new ArrayList<>();
                datos.add("documento");
                datos.add("serie");
                datos.add("ruc");
                datos.add("titulo");
                for (int p = 0; p < despatchAdvice.getTransaccion().getTransactionDocReferDTOList().size(); p++) {
                    WrapperItemObject itemObject = new WrapperItemObject();
                    Map<String, String> itemObjectHash = new HashMap<>();
                    List<String> newlist = new ArrayList<>();
                    for (int y = 0; y < datos.size(); y++) {
                        switch (datos.get(y)) {
                            case "documento":
                                switch (despatchAdvice.getTransaccion().getTransactionDocReferDTOList().get(p).getTipo()) {
                                    case "01":
                                        nombreDocumento = "Factura";
                                        break;
                                    case "03":
                                        nombreDocumento = "Boleta";
                                        break;
                                    case "07":
                                        nombreDocumento = "Nota de Credito";
                                        break;
                                    case "08":
                                        nombreDocumento = "Nota de Debito";
                                        break;
                                    case "09":
                                        nombreDocumento = "Guía de Remisión";
                                        break;
                                    default:
                                        nombreDocumento = "";
                                        break;
                                }
                                itemObjectHash.put(datos.get(y), nombreDocumento);
                                break;
                            case "serie":
                                itemObjectHash.put(datos.get(y), despatchAdvice.getTransaccion().getTransactionDocReferDTOList().get(p).getId());
                                break;
                            case "ruc":
                                itemObjectHash.put(datos.get(y), despatchAdvice.getTransaccion().getTransactionGuias().getGRT_DocumentoRemitente());
                                break;
                            case "titulo":
                                itemObjectHash.put(datos.get(y), "");
                                break;
                        }
                    }
                    newlist.add(despatchAdvice.getTransaccion().getTransactionDocReferDTOList().get(p).getId());
                    itemObject.setLstItemHashMap(itemObjectHash);
                    itemObject.setLstDinamicaItem(newlist);
                    listaItem.add(itemObject);
                }
                WrapperItemObject itemObject2 = new WrapperItemObject();
                Map<String, String> itemObjectHash2 = new HashMap<>();
                List<String> newlist2 = new ArrayList<>();
                newlist2.add("");
                itemObjectHash2.put("documento", "");
                itemObjectHash2.put("serie", "");
                itemObjectHash2.put("ruc", "");
                itemObjectHash2.put("titulo", "Bienes por Transportar:");
                itemObject2.setLstItemHashMap(itemObjectHash2);
                itemObject2.setLstDinamicaItem(newlist2);
                listaItem.add(itemObject2);

                WrapperItemObject itemObject3 = new WrapperItemObject();
                Map<String, String> itemObjectHash3 = new HashMap<>();
                itemObjectHash3.put("documento", "<style forecolor='#000000' isBold='true'>Cantidad</style>");
                itemObjectHash3.put("serie", "<style forecolor='#000000' isBold='true'>Unidad</style>");
                itemObjectHash3.put("ruc", "<style forecolor='#000000' isBold='true'>Descripción</style>");
                itemObjectHash3.put("titulo", "");
                itemObject3.setLstItemHashMap(itemObjectHash3);
                itemObject3.setLstDinamicaItem(newlist2);
                listaItem.add(itemObject3);

                for (int i = 0; i < despatchAdvice.getTransaccion().getTransactionLineasDTOList().size(); i++) {
                    WrapperItemObject itemObject = new WrapperItemObject();
                    Map<String, String> itemObjectHash = new HashMap<>();
                    List<String> newlist = new ArrayList<>();
                    for (int y = 0; y < datos.size(); y++) {
                        switch (datos.get(y)) {
                            case "documento":
                                itemObjectHash.put(datos.get(y), despatchAdvice.getTransaccion().getTransactionLineasDTOList().get(i).getCantidad().toString());
                                break;
                            case "serie":
                                itemObjectHash.put(datos.get(y), despatchAdvice.getTransaccion().getTransactionLineasDTOList().get(i).getUnidad());
                                break;
                            case "ruc":
                                itemObjectHash.put(datos.get(y), despatchAdvice.getTransaccion().getTransactionLineasDTOList().get(i).getDescripcion());
                                break;
                            case "titulo":
                                itemObjectHash.put(datos.get(y), "");
                                break;
                        }
                    }
                    newlist.add(despatchAdvice.getTransaccion().getTransactionLineasDTOList().get(i).getDescripcion());
                    itemObject.setLstItemHashMap(itemObjectHash);
                    itemObject.setLstDinamicaItem(newlist);
                    listaItem.add(itemObject);
                }
            } else {
                /** */
                for (int i = 0; i < despatchAdvice.getTransaccion().getTransactionLineasDTOList().size(); i++) {

                    WrapperItemObject itemObject = new WrapperItemObject();
                    Map<String, String> itemObjectHash = new HashMap<>();
                    List<String> newlist = new ArrayList<>();

                    // Obtener el mapa de transaccionLineasCamposUsuario
                    Map<String, String> camposUsuarioMap = despatchAdvice.getTransaccion().getTransactionLineasDTOList().get(i).getTransaccionLineasCamposUsuario();

                    if (camposUsuarioMap != null && !camposUsuarioMap.isEmpty()) {
                        // Iterar sobre las entradas del mapa
                        for (Map.Entry<String, String> entry : camposUsuarioMap.entrySet()) {
                            String nombreCampo = entry.getKey();
                            String valorCampo = entry.getValue();

                            // Si el campo empieza con "U_", se elimina antes de guardarlo
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
            }

            if (despatchAdvice.getTransaccion().getTransactionContractDocRefListDTOS() != null
                    && !despatchAdvice.getTransaccion().getTransactionContractDocRefListDTOS().isEmpty()) {

                Map<String, String> hashedMap = new HashMap<>();

                // Iterar sobre cada mapa en la lista
                for (Map<String, String> map : despatchAdvice.getTransaccion().getTransactionContractDocRefListDTOS()) {
                    // Asumimos que cada mapa contiene un único par clave-valor
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        hashedMap.put(entry.getKey(), entry.getValue());
                    }
                }

                despatchAdviceObject.setDespatchAdvicePersonalizacion(hashedMap);
            }

            despatchAdviceObject.setNumeroDocEmisor(despatchAdvice.getTransaccion().getDocIdentidad_Nro());
            despatchAdviceObject.setNumeroGuia(despatchAdvice.getAdviceType().getID().getValue());
            despatchAdviceObject.setTelefono(despatchAdvice.getTransaccion().getTelefono());
            despatchAdviceObject.setTelefono1(despatchAdvice.getTransaccion().getTelefono_1());
            despatchAdviceObject.setEmail(despatchAdvice.getTransaccion().getEMail());
            despatchAdviceObject.setPaginaWeb(despatchAdvice.getTransaccion().getWeb());
            despatchAdviceObject.setObervaciones(despatchAdvice.getTransaccion().getObservacione());
            despatchAdviceObject.setItemListDynamic(listaItem);

            String barcodeValue = generateBarCodeInfoString(despatchAdvice.getTransaccion().getDocIdentidad_Nro(), despatchAdvice.getTransaccion().getDOC_Codigo(), despatchAdvice.getTransaccion().getDOC_Serie(), despatchAdvice.getTransaccion().getDOC_Numero(), null, despatchAdvice.getTransaccion().getDOC_FechaEmision().toString(), "00", despatchAdvice.getTransaccion().getSN_DocIdentidad_Tipo(), despatchAdvice.getTransaccion().getSN_DocIdentidad_Nro(), despatchAdvice.getAdviceType().getUBLExtensions());

            InputStream inputStream;

            File f = new File(".." + File.separator + "ADJUNTOS" + File.separator + "CodigoPDF417" + File.separator + "09");
            String rutaPath = ".." + File.separator + "ADJUNTOS" + File.separator + "CodigoPDF417" + File.separator + "09" + File.separator + despatchAdvice.getTransaccion().getDOC_Numero() + ".png";
            if (!f.exists()) {
                f.mkdirs();
            }

            if(configData.getUrlGuias() != null )
                inputStream = generateQRCode(configData.getUrlGuias(), rutaPath);
            else {
                inputStream = generateQRCode(barcodeValue, rutaPath);
            }

            despatchAdviceObject.setCodeQR(inputStream);

            despatchAdviceObject.setSenderLogo(configData.getCompletePathLogo());

            despatchInBytes = createDespatchAdvicePDF(despatchAdviceObject, docUUID, configData, despatchAdvice.getTransaccion().getDOC_Codigo(), personalizacion);

        } catch (Exception e) {
            throw new PDFReportException(e.getMessage());
        }
        return despatchInBytes;
    }

    public byte[] createDespatchAdvicePDF(DespatchAdviceObject despatchAdviceObject, String docUUID, ConfigData configuracion, String docCodigo, String personalizacion) throws PDFReportException {

        Map<String, Object> parameterMap;
        Map<String, Object> cuotasMap;

        byte[] pdfDocument = null;

        if (null == despatchAdviceObject) {
            throw new PDFReportException(IVenturaError.ERROR_406);
        } else {
            try {

                parameterMap = new HashMap<String, Object>();
                parameterMap.put(IPDFCreatorConfig.CODIGO_EMBARQUE, despatchAdviceObject.getCodigoEmbarque());
                parameterMap.put(IPDFCreatorConfig.CODIGO_MOTIVO, despatchAdviceObject.getCodigoMotivoTraslado());
                parameterMap.put(IPDFCreatorConfig.DESCRIPCION_MOTIVO, despatchAdviceObject.getDescripcionMotivoTraslado());
                parameterMap.put(IPDFCreatorConfig.DIRECCION_DESTINO, despatchAdviceObject.getDireccionDestino());
                parameterMap.put(IPDFCreatorConfig.DIRECCION_PARTIDA, despatchAdviceObject.getDireccionPartida());
                parameterMap.put(IPDFCreatorConfig.DOCUMENTO_CONDUCTOR, despatchAdviceObject.getDocumentoConductor());
                parameterMap.put(IPDFCreatorConfig.FECHA_EMISION, despatchAdviceObject.getFechaEmision());
                parameterMap.put(IPDFCreatorConfig.FECHA_TRASLADO, despatchAdviceObject.getFechaTraslado());
                parameterMap.put(IPDFCreatorConfig.MODALIDAD_TRASLADO, despatchAdviceObject.getModalidadTraslado());
                parameterMap.put(IPDFCreatorConfig.NOMBRE_CONSUMIDOR, despatchAdviceObject.getNombreConsumidor());
                parameterMap.put(IPDFCreatorConfig.NOMBRE_EMISOR, despatchAdviceObject.getNombreEmisor());
                parameterMap.put(IPDFCreatorConfig.NOMBRE_TRANSPORTISTA, despatchAdviceObject.getNombreTransportista());
                parameterMap.put(IPDFCreatorConfig.NUMERO_BULTOS, despatchAdviceObject.getNumeroBultos());
                parameterMap.put(IPDFCreatorConfig.PLACA_VEHICULO, despatchAdviceObject.getPlacaVehiculo());
                parameterMap.put(IPDFCreatorConfig.RUC_TRANSPORTISTA, despatchAdviceObject.getRUCTransportista());
                parameterMap.put(IPDFCreatorConfig.TIPO_DOCUMENTO_CONDUCTOR, despatchAdviceObject.getTipoDocumentoConductor());
                parameterMap.put(IPDFCreatorConfig.TIPO_DOCUMENTO_TRANSPORTISTA, despatchAdviceObject.getTipoDocumentoTransportista());
                parameterMap.put(IPDFCreatorConfig.UNIDAD_MEDIDA_PESONETO, despatchAdviceObject.getUMPesoBruto());
                /** 04-03-2024  Harol Peso Guia Transportista*/
                parameterMap.put(IPDFCreatorConfig.PESONETO, despatchAdviceObject.getPesoBruto());
                /** */
                parameterMap.put(IPDFCreatorConfig.VALIDEZPDF, despatchAdviceObject.getValidezPDF());
                parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER, despatchAdviceObject.getNumeroGuia());
                parameterMap.put(IPDFCreatorConfig.SENDER_RUC, despatchAdviceObject.getNumeroDocEmisor());
                parameterMap.put(IPDFCreatorConfig.SENDER_MAIL, despatchAdviceObject.getEmail());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL, despatchAdviceObject.getTelefono());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL_1, despatchAdviceObject.getTelefono1());
                parameterMap.put(IPDFCreatorConfig.SENDER_WEB, despatchAdviceObject.getPaginaWeb());
                parameterMap.put(IPDFCreatorConfig.COMMENTS, despatchAdviceObject.getObervaciones());
                parameterMap.put(IPDFCreatorConfig.RUC_CONSUMIDOR, despatchAdviceObject.getNumeroDocConsumidor());
                parameterMap.put(IPDFCreatorConfig.RUC_EMISOR, despatchAdviceObject.getNumeroDocEmisor());
                parameterMap.put(IPDFCreatorConfig.SENDER_LOGO_PATH, despatchAdviceObject.getSenderLogo());
                parameterMap.put(IPDFCreatorConfig.LICENCIA_CONDUCIR, despatchAdviceObject.getLicenciaConducir());
                parameterMap.put(IPDFCreatorConfig.CAMPOS_USUARIO_CAB, despatchAdviceObject.getDespatchAdvicePersonalizacion());

                /** 29-02-2024 Harol Guia Transportista*/
                parameterMap.put(IPDFCreatorConfig.GRT_DOCUMENTO_REMITENTE, despatchAdviceObject.getGrtDocumentoRemitente());
                parameterMap.put(IPDFCreatorConfig.GRT_NOMBRE_RAZON_REMITENTE, despatchAdviceObject.getGrtNombreRazonRemitente());
                parameterMap.put(IPDFCreatorConfig.GRT_DOCUMENTO_DESTINATARIO, despatchAdviceObject.getGrtDocumentoDestinatario());
                parameterMap.put(IPDFCreatorConfig.GRT_NOMBRE_RAZON_DESTINATARIO, despatchAdviceObject.getGrtNombreRazonDestinatario());
                parameterMap.put(IPDFCreatorConfig.NRO_REGISTRO_MTC, despatchAdviceObject.getNroRegistroMtc());
                parameterMap.put(IPDFCreatorConfig.SN_DOC_IDENTIDAD_NRO, despatchAdviceObject.getSnDocIdentidadNro());
                parameterMap.put(IPDFCreatorConfig.SN_RAZON_SOCIAL, despatchAdviceObject.getSnRazonSocial());
                parameterMap.put(IPDFCreatorConfig.NOMBRE_CONDUCTOR, despatchAdviceObject.getNombreConductor());

                parameterMap.put(IPDFCreatorConfig.INDICADOR_TRANSBORDO_PROGRAMADO, despatchAdviceObject.getIndicadorTransbordoProgramado());
                parameterMap.put(IPDFCreatorConfig.INDICADOR_RETORNO_VEHICULO_VACIO, despatchAdviceObject.getIndicadorRetornoVehiculoVacio());
                parameterMap.put(IPDFCreatorConfig.INDICADOR_TRANSPORTE_SUBCONTRATADO, despatchAdviceObject.getIndicadorTransporteSubcontratado());
                parameterMap.put(IPDFCreatorConfig.INDICADOR_RETORNO_VEHICULO_ENVASES_VACIO, despatchAdviceObject.getIndicadorRetornoVehiculoEnvasesVacio());
                parameterMap.put(IPDFCreatorConfig.GRT_INDICADOR_PAGADOR_FLETE, despatchAdviceObject.getGrtIndicadorPagadorFlete());

                parameterMap.put(IPDFCreatorConfig.GRT_NUMERO_TUCE_PRINCIPAL, despatchAdviceObject.getGrtNumeroTUCEPrincipal());
                parameterMap.put(IPDFCreatorConfig.GRT_ENTIDAD_EMISORA_PRINCIPAL, despatchAdviceObject.getGrtEntidadEmisoraPrincipal());
                parameterMap.put(IPDFCreatorConfig.GRT_PLACA_VEHICULO_SECUNDARIO, despatchAdviceObject.getGrtPlacaVehiculoSecundario());
                parameterMap.put(IPDFCreatorConfig.GRT_NUMERO_TUCE_SECUNDARIO, despatchAdviceObject.getGrtNumeroTUCESecuendario());
                parameterMap.put(IPDFCreatorConfig.GRT_ENTIDAD_EMISORA_SECUNDARIO, despatchAdviceObject.getGrtEntidadEmisoraSecundario());
                /** */

                if (configuracion.getImpresionPDF().equalsIgnoreCase("Codigo QR")) {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, despatchAdviceObject.getCodeQR());
                } else {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, null);
                }
                String documentName = null;
                if ("09".equals(docCodigo)) {
                    if (personalizacion != null && !personalizacion.isEmpty()) {
                        documentName = "remissionguideDocument_" + personalizacion + ".jrxml";
                    }else {
                        documentName = (configuracion.getPdfIngles() != null && configuracion.getPdfIngles().equals("Si")) ? "remissionguideDocument_Ing.jrxml" : "remissionguideDocument.jrxml";
                    }
                } else if ("31".equals(docCodigo)) {
                    documentName = (configuracion.getPdfIngles() != null && configuracion.getPdfIngles().equals("Si")) ? "carrierguideDocument_Ing.jrxml" : "carrierguideDocument.jrxml";
                }

                JasperReport jasperReport = jasperReportConfig.getJasperReportForRuc(despatchAdviceObject.getNumeroDocEmisor(), documentName, configuracion.getRutaBaseConfig());

                JasperPrint iJasperPrint = JasperFillManager.fillReport(jasperReport, parameterMap,
                        new JRBeanCollectionDataSource(despatchAdviceObject.getItemListDynamic()));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
                pdfDocument = outputStream.toByteArray();
            } catch (Exception e) {
                throw new PDFReportException(e.getMessage());
            }
        }
        return pdfDocument;
    } //createInvoicePDF

}
