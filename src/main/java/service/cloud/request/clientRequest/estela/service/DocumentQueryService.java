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
public class DocumentQueryService {

    private final ServiceProxy serviceClient;
    private final DocumentBuilder soapRequestBuilder;

    @Inject
    public DocumentQueryService(ServiceProxy serviceClient, DocumentBuilder soapRequestBuilder) {
        this.serviceClient = serviceClient;
        this.soapRequestBuilder = soapRequestBuilder;
    }

    public Uni<FileResponseDTO> processAndSaveFile(String url, FileRequestDTO soapRequest) {
        String body = "BIZLINKS".equalsIgnoreCase(soapRequest.getIntegracionWs())
                ? soapRequestBuilder.buildConsultaSoapRequestBizlinks(soapRequest)
                : soapRequestBuilder.buildConsultaSoapRequest(soapRequest);
        System.out.println(body);
        return serviceClient.sendSoapRequest(url, body)
                .chain(this::handleSoapResponse)
                .onFailure().recoverWithUni(error -> {
                    String errorMessage = error.getMessage() != null ? error.getMessage() : "Unknown error";
                    return Uni.createFrom().item(new FileResponseDTO("Error", errorMessage, null, null));
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

    private String extractApplicationResponseWithRegex(String soapResponse) {
        Pattern pattern = Pattern.compile("<(?:content|document)>(.*?)</(?:content|document)>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(soapResponse);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException("No se encontró <applicationResponse> en la respuesta SOAP.");
    }
}
