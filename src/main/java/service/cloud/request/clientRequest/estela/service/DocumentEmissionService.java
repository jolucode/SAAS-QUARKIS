package service.cloud.request.clientRequest.estela.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.estela.builder.DocumentBuilder;
import service.cloud.request.clientRequest.estela.dto.FileRequestDTO;
import service.cloud.request.clientRequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientRequest.estela.proxy.ServiceProxy;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentEmissionService {

    private final ServiceProxy serviceProxy;

    private final DocumentBuilder soapRequestBuilder;

    @Autowired
    public DocumentEmissionService(ServiceProxy serviceProxy, DocumentBuilder soapRequestBuilder) {
        this.serviceProxy = serviceProxy;
        this.soapRequestBuilder = soapRequestBuilder;
    }

    public Mono<FileResponseDTO> processDocumentEmission(String url, FileRequestDTO soapRequest) {
        return serviceProxy.sendSoapRequest(url, soapRequestBuilder.buildEmissionSoapRequest(soapRequest))
                .flatMap(this::handleSoapResponse)
                .onErrorResume(error -> {
                    String errorMessage = extractErrorMessage(error.getMessage());
                    String cleanErrorMessage = errorMessage.replace("\\", "").replace("\"", "'");
                    return Mono.just(new FileResponseDTO("Error", cleanErrorMessage, null,null));
                });
    }

    private Mono<FileResponseDTO> handleSoapResponse(String soapResponse) {
        if (soapResponse.contains("<soap-env:Fault") || soapResponse.contains("<faultstring xml:lang=\"es-PE\">")) {
            return Mono.error(new RuntimeException("Error en SUNAT: " + soapResponse));
        }
        String base64Content = extractApplicationResponseWithRegex(soapResponse);
        byte[] originalBytes = convertFromBase64(base64Content);
        return Mono.just(new FileResponseDTO("Success", "File processed successfully", originalBytes,null));
    }

    private static byte[] convertFromBase64(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }

    public static String extractApplicationResponseWithRegex(String soapResponse) {
        // Regex que busca el contenido entre <applicationResponse> y </applicationResponse>
        Pattern pattern = Pattern.compile("<applicationResponse[^>]*>(.*?)</applicationResponse>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(soapResponse);

        if (matcher.find()) {
            return matcher.group(1).trim();
        } else {
            // Puedes lanzar una excepción o manejar el caso donde no se encuentra la etiqueta
            throw new IllegalArgumentException("No se encontró applicationResponse en el contenido SOAP.");
        }
    }

    private String extractErrorMessage(String xmlResponse) {
        try {

            org.jsoup.nodes.Document document = org.jsoup.Jsoup.parse(xmlResponse, "", org.jsoup.parser.Parser.xmlParser());

            String message = document.select("message").text();
            if (!message.isEmpty()) {
                return message;
            }

            String faultString = document.select("faultstring").text();
            if (!faultString.isEmpty()) {
                return faultString;
            }
            return "Error no especificado en la respuesta";

        } catch (Exception e) {
            return "Error no especificado";
        }
    }
}

