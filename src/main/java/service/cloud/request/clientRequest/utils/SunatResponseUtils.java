package service.cloud.request.clientRequest.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.persistence.internal.oxm.ByteArraySource;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.utils.files.UtilsFile;
import service.cloud.request.clientRequest.xmlFormatSunat.uncefact.data.specification.corecomponenttypeschemamodule._2.TextType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.applicationresponse_2.ApplicationResponseType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.DocumentResponseType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.ResponseType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.StatusType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.DescriptionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.ResponseCodeType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.StatusReasonCodeType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.StatusReasonType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SunatResponseUtils {

    public static TransaccionRespuesta.Sunat proccessResponse(byte[] cdrConstancy, TransacctionDTO transaction, String sunatType) {
        try {
            String descripcionRespuesta = "";
            Optional<byte[]> unzipedResponse = unzipResponse(cdrConstancy);
            int codigoObservacion = 0;
            int codigoRespuesta = 0;
            String identificador = Constants.IDENTIFICATORID_OSE;
            if (unzipedResponse.isPresent()) {
                StringBuilder descripcion = new StringBuilder();
                JAXBContext jaxbContext = JAXBContext.newInstance(ApplicationResponseType.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                JAXBElement<ApplicationResponseType> jaxbElement = unmarshaller.unmarshal(new ByteArraySource(unzipedResponse.get()), ApplicationResponseType.class);
                ApplicationResponseType applicationResponse = jaxbElement.getValue();
                List<DocumentResponseType> documentResponse = applicationResponse.getDocumentResponse();
                List<TransaccionRespuesta.Observacion> observaciones = new ArrayList<>();
                for (DocumentResponseType documentResponseType : documentResponse) {
                    ResponseType response = documentResponseType.getResponse();
                    ResponseCodeType responseCode = response.getResponseCode();
                    codigoRespuesta = Optional.ofNullable(responseCode.getValue()).map(s -> s.isEmpty() ? null : s).map(Integer::parseInt).orElse(0);
                    List<DescriptionType> descriptions = response.getDescription();
                    for (DescriptionType description : descriptions) {
                        descripcion.append(description.getValue());
                    }
                    if (sunatType.equalsIgnoreCase(Constants.IDENTIFICATORID_OSE)
                            || sunatType.equalsIgnoreCase(Constants.IDENTIFICATORID_ESTELA)
                            || sunatType.equalsIgnoreCase(Constants.IDENTIFICATORID_BIZLINKS)) {
                        identificador = documentResponseType.getDocumentReference().getID().getValue();
                    } else {
                        identificador = documentResponseType.getResponse().getReferenceID().getValue();
                    }
                    List<StatusType> statusTypes = response.getStatus();
                    for (StatusType statusType : statusTypes) {
                        List<StatusReasonType> statusReason = statusType.getStatusReason();
                        String mensajes = statusReason.parallelStream().map(TextType::getValue).collect(Collectors.joining("\n"));
                        StatusReasonCodeType statusReasonCode = statusType.getStatusReasonCode();
                        codigoObservacion = Optional.ofNullable(statusReasonCode.getValue()).map(s -> s.isEmpty() ? null : s).map(Integer::parseInt).orElse(0);
                        TransaccionRespuesta.Observacion observacion = new TransaccionRespuesta.Observacion();
                        observacion.setCodObservacion(codigoObservacion);
                        observacion.setMsjObservacion(mensajes);
                        observaciones.add(observacion);
                    }
                }
                descripcionRespuesta = descripcion.toString();
                TransaccionRespuesta.Sunat sunatResponse = new TransaccionRespuesta.Sunat();
                sunatResponse.setListaObs(observaciones);
                sunatResponse.setId(identificador);
                sunatResponse.setCodigo(codigoRespuesta);
                sunatResponse.setMensaje(descripcionRespuesta);
                sunatResponse.setEmisor(transaction.getDocIdentidad_Nro());
                return sunatResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new TransaccionRespuesta.Sunat();
    }

    public static String proccessResponseUrlPdfGuia(byte[] cdrConstancy) {
        try {
            Optional<byte[]> unzippedResponse = unzipResponse(cdrConstancy);
            if (unzippedResponse.isEmpty()) {
                return null;
            }

            String xmlContent = new String(unzippedResponse.get(), StandardCharsets.UTF_8);

            Pattern pattern = Pattern.compile("<cbc:DocumentDescription>(.*?)</cbc:DocumentDescription>");
            Matcher matcher = pattern.matcher(xmlContent);

            if (matcher.find()) {
                return matcher.group(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String extractDigestValue(byte[] cdrConstancy) {
        try {
            Optional<byte[]> unzippedResponse = unzipResponse(cdrConstancy);
            if (unzippedResponse.isEmpty()) {
                return null;
            }

            String xmlContent = new String(unzippedResponse.get(), StandardCharsets.UTF_8);

            // Regex para capturar el contenido entre <DigestValue>...</DigestValue>
            Pattern pattern = Pattern.compile("<[^>]*:?DigestValue>(.*?)</[^>]*:?DigestValue>");
            Matcher matcher = pattern.matcher(xmlContent);

            if (matcher.find()) {
                return matcher.group(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Optional<byte[]> unzipResponse(byte[] cdr) throws IOException {
        if (cdr == null || cdr.length == 0) {
            Logger logger = LoggerFactory.getLogger(SunatResponseUtils.class);
            logger.warn("Respuesta de SUNAT vacía o null");
            return Optional.empty();
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(cdr);
        ZipInputStream zis = new ZipInputStream(bais);
        ZipEntry entry = zis.getNextEntry();
        byte[] xml = null;
        if (entry != null) { // valida dos veces lo mismo
            while (entry != null) {
                if (!entry.isDirectory()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] bytesIn = new byte['?'];
                    int read;
                    while ((read = zis.read(bytesIn)) != -1) {
                        baos.write(bytesIn, 0, read);
                    }
                    baos.close();
                    xml = baos.toByteArray();
                }
                zis.closeEntry();
                entry = zis.getNextEntry();
            }
            zis.close();
            return Optional.ofNullable(xml);
        } else {
            zis.close();
            return Optional.empty();
        }
    }

}

