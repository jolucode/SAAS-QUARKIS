package service.cloud.request.clientRequest.estela.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.cloud.request.clientRequest.estela.builder.DocumentBuilder;
import service.cloud.request.clientRequest.estela.dto.FileRequestDTO;
import service.cloud.request.clientRequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientRequest.estela.proxy.ServiceProxy;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@ApplicationScoped
public class DocumentBajaQueryService {

    Logger logger = LoggerFactory.getLogger(DocumentBajaQueryService.class);

    private final ServiceProxy serviceClient;
    private final DocumentBuilder soapRequestBuilder;

    @Inject
    public DocumentBajaQueryService(ServiceProxy serviceClient, DocumentBuilder soapRequestBuilder) {
        this.serviceClient = serviceClient;
        this.soapRequestBuilder = soapRequestBuilder;
    }

    public Uni<FileResponseDTO> processAndSaveFile(String url, FileRequestDTO soapRequest) {
        String soapRequestXml = soapRequestBuilder.buildConsultaBajasSoapRequest(soapRequest).replaceAll("\\s+", " ");
        logger.info("EMISION BAJA CONSULTA: {}-{}-{}-{}", soapRequest.getRucComprobante(), soapRequest.getTipoComprobante(),
                soapRequest.getSerieComprobante(), soapRequest.getNumeroComprobante());
        logger.info(soapRequestXml);

        return serviceClient.sendSoapRequest(url, soapRequestBuilder.buildConsultaBajasSoapRequest(soapRequest))
                .chain(this::handleSoapResponse)
                .onFailure().recoverWithUni(error -> {
                    String errorMessage = extractErrorMessage(error.getMessage());
                    String cleanErrorMessage = errorMessage.replace("\\", "").replace("\"", "'");
                    return Uni.createFrom().item(new FileResponseDTO("Error", cleanErrorMessage, null, null));
                });
    }

    private Uni<FileResponseDTO> handleSoapResponse(String soapResponse) {
        if (soapResponse.contains("SOAP-ENV:Fault") || soapResponse.contains("<soap-env:Fault")
                || soapResponse.contains("<faultstring xml:lang=\"es-PE\">")
                || soapResponse.contains("<statusCode>98</statusCode>")) {
            return Uni.createFrom().failure(new RuntimeException("Error en SUNAT: " + soapResponse));
        }
        String base64Content = extractApplicationResponseWithRegex(soapResponse);
        byte[] originalBytes = Base64.getDecoder().decode(base64Content);
        return Uni.createFrom().item(new FileResponseDTO("Success", "File processed successfully", originalBytes, null));
    }

    private String extractApplicationResponseWithRegex(String soapResponse) {
        Pattern pattern = Pattern.compile("<content>(.*?)</content>");
        Matcher matcher = pattern.matcher(soapResponse);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException("No se encontró <content> en la respuesta SOAP.");
    }

    private String extractErrorMessage(String xmlResponse) {
        try {
            org.jsoup.nodes.Document document = org.jsoup.Jsoup.parse(xmlResponse, "", org.jsoup.parser.Parser.xmlParser());
            String message = document.select("message").text();
            if (!message.isEmpty()) return message;
            String faultString = document.select("faultstring").text();
            if (!faultString.isEmpty()) return faultString;
            String statusCode = document.select("statusCode").text();
            if ("98".equals(statusCode)) return "SUNAT aún está procesando el comprobante (código 98)";
            return "";
        } catch (Exception e) {
            return "Error no especificado";
        }
    }
}
