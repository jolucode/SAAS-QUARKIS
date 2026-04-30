package service.cloud.request.clientRequest.estela.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.cloud.request.clientRequest.estela.builder.DocumentBuilder;
import service.cloud.request.clientRequest.estela.dto.FileRequestDTO;
import service.cloud.request.clientRequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientRequest.estela.proxy.ServiceProxy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class DocumentBajaService {

    Logger logger = LoggerFactory.getLogger(DocumentBajaService.class);

    private final ServiceProxy serviceClient;
    private final DocumentBuilder soapRequestBuilder;

    @Inject
    public DocumentBajaService(ServiceProxy serviceClient, DocumentBuilder soapRequestBuilder) {
        this.serviceClient = serviceClient;
        this.soapRequestBuilder = soapRequestBuilder;
    }

    public Uni<FileResponseDTO> processBajaRequest(String url, FileRequestDTO soapRequest) {
        String soapRequestXml = soapRequestBuilder.buildEmisionBajaSoapRequest(soapRequest).replaceAll("\\s+", " ");
        logger.info("EMISION BAJA: {}-{}-{}-{}", soapRequest.getRucComprobante(), soapRequest.getTipoComprobante(),
                soapRequest.getSerieComprobante(), soapRequest.getNumeroComprobante());
        logger.info(soapRequestXml);

        return serviceClient.sendSoapRequest(url, soapRequestBuilder.buildEmisionBajaSoapRequest(soapRequest))
                .chain(this::handleBajaResponse)
                .onFailure().recoverWithUni(error -> {
                    String errorMessage = error.getMessage() != null ? error.getMessage() : "Unknown error";
                    return Uni.createFrom().item(new FileResponseDTO("Error", errorMessage, null, null));
                });
    }

    private Uni<FileResponseDTO> handleBajaResponse(String soapResponse) {
        if (soapResponse.contains("<soap-env:Fault") || soapResponse.contains("<faultstring xml:lang=\"es-PE\">")) {
            return Uni.createFrom().failure(new RuntimeException("Error en SUNAT: " + soapResponse));
        }
        String ticket = extractTicketFromResponse(soapResponse);
        return Uni.createFrom().item(new FileResponseDTO("Success", "Ticket retrieved successfully", null, ticket));
    }

    private String extractTicketFromResponse(String soapResponse) {
        Pattern pattern = Pattern.compile("<ticket>(.*?)</ticket>");
        Matcher matcher = pattern.matcher(soapResponse);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException("No se encontró <ticket> en la respuesta SOAP.");
    }
}
