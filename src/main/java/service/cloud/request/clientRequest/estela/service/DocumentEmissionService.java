package service.cloud.request.clientRequest.estela.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import service.cloud.request.clientRequest.estela.builder.DocumentBuilder;
import service.cloud.request.clientRequest.estela.dto.FileRequestDTO;
import service.cloud.request.clientRequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientRequest.estela.proxy.ServiceProxy;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class DocumentEmissionService {

    private final ServiceProxy serviceProxy;
    private final DocumentBuilder soapRequestBuilder;

    @Inject
    public DocumentEmissionService(ServiceProxy serviceProxy, DocumentBuilder soapRequestBuilder) {
        this.serviceProxy = serviceProxy;
        this.soapRequestBuilder = soapRequestBuilder;
    }

    public Uni<FileResponseDTO> processDocumentEmission(String url, FileRequestDTO soapRequest) {
        return serviceProxy.sendSoapRequest(url, soapRequestBuilder.buildEmissionSoapRequest(soapRequest))
                .chain(this::handleSoapResponse)
                .onFailure().recoverWithUni(error -> {
                    String errorMessage = extractErrorMessage(error.getMessage());
                    String cleanErrorMessage = errorMessage.replace("\\", "").replace("\"", "'");
                    return Uni.createFrom().item(new FileResponseDTO("Error", cleanErrorMessage, null, null));
                });
    }

    private Uni<FileResponseDTO> handleSoapResponse(String soapResponse) {
        if (soapResponse.contains("<soap-env:Fault") || soapResponse.contains("<faultstring xml:lang=\"es-PE\">")) {
            return Uni.createFrom().failure(new RuntimeException("Error en SUNAT: " + soapResponse));
        }
        String base64Content = extractApplicationResponseWithRegex(soapResponse);
        byte[] originalBytes = Base64.getDecoder().decode(base64Content);
        return Uni.createFrom().item(new FileResponseDTO("Success", "File processed successfully", originalBytes, null));
    }

    public static String extractApplicationResponseWithRegex(String soapResponse) {
        Pattern pattern = Pattern.compile("<applicationResponse[^>]*>(.*?)</applicationResponse>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(soapResponse);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        throw new IllegalArgumentException("No se encontró applicationResponse en el contenido SOAP.");
    }

    private String extractErrorMessage(String xmlResponse) {
        try {
            org.jsoup.nodes.Document document = org.jsoup.Jsoup.parse(xmlResponse, "", org.jsoup.parser.Parser.xmlParser());
            String message = document.select("message").text();
            if (!message.isEmpty()) return message;
            String faultString = document.select("faultstring").text();
            if (!faultString.isEmpty()) return faultString;
            return "Error no especificado en la respuesta";
        } catch (Exception e) {
            return "Error no especificado";
        }
    }
}
