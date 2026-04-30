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
public class DocumentQueryService {

    private final ServiceProxy serviceClient;

    private final DocumentBuilder soapRequestBuilder;

    @Autowired
    public DocumentQueryService(ServiceProxy serviceClient, DocumentBuilder soapRequestBuilder) {
        this.serviceClient = serviceClient;
        this.soapRequestBuilder = soapRequestBuilder;
    }

    public Mono<FileResponseDTO> processAndSaveFile(String url, FileRequestDTO soapRequest) {
        String body = "BIZLINKS".equalsIgnoreCase(soapRequest.getIntegracionWs())
                ? soapRequestBuilder.buildConsultaSoapRequestBizlinks(soapRequest)
                : soapRequestBuilder.buildConsultaSoapRequest(soapRequest);
        System.out.println(body);
        return serviceClient.sendSoapRequest(url, body)
                .flatMap(this::handleSoapResponse)
                .onErrorResume(error -> {
                    String errorMessage = error.getMessage() != null ? error.getMessage() : "Unknown error";
                    return Mono.just(new FileResponseDTO("Error", errorMessage, null,null));
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

    private String extractApplicationResponseWithRegex(String soapResponse) {
        Pattern pattern = Pattern.compile("<(?:content|document)>(.*?)</(?:content|document)>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(soapResponse);
        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new RuntimeException("No se encontró <applicationResponse> en la respuesta SOAP.");
    }
}

