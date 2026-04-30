package service.cloud.request.clientRequest.estela.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Slf4j
@Service
public class DocumentBajaQueryService {

    Logger logger = LoggerFactory.getLogger(DocumentBajaQueryService.class);

    private final ServiceProxy serviceClient;

    private final DocumentBuilder soapRequestBuilder;

    @Autowired
    public DocumentBajaQueryService(ServiceProxy serviceClient, DocumentBuilder soapRequestBuilder) {
        this.serviceClient = serviceClient;
        this.soapRequestBuilder = soapRequestBuilder;
    }

    public Mono<FileResponseDTO> processAndSaveFile(String url, FileRequestDTO soapRequest) {

        System.out.println("");
        logger.info("EMISION BAJA CONSULTA: " + soapRequest.getRucComprobante() + "-" + soapRequest.getTipoComprobante() + "-" +soapRequest.getSerieComprobante() + "-" + soapRequest.getNumeroComprobante());
        String soapRequestXml = soapRequestBuilder.buildConsultaBajasSoapRequest(soapRequest).replaceAll("\\s+", " "); // Elimina saltos de línea, tabs, etc., y los reemplaza por un espacio
        logger.info(soapRequestXml);
        System.out.println("");


        return serviceClient.sendSoapRequest(url, soapRequestBuilder.buildConsultaBajasSoapRequest(soapRequest))

                .flatMap(this::handleSoapResponse)
                .onErrorResume(error -> {
                    //System.out.println("Error en .onErrorResume(error -> { " + error.getMessage());
                    String errorMessage = extractErrorMessage(error.getMessage());
                    String cleanErrorMessage = errorMessage.replace("\\", "").replace("\"", "'");
                    return Mono.just(new FileResponseDTO("Error", cleanErrorMessage, null,null));
                });
    }

    private Mono<FileResponseDTO> handleSoapResponse(String soapResponse) {
        //System.out.println("handleSoapResponse() : " +  soapResponse);
        if (soapResponse.contains("SOAP-ENV:Fault") || soapResponse.contains("<soap-env:Fault") || soapResponse.contains("<faultstring xml:lang=\"es-PE\">") ||
                soapResponse.contains("<statusCode>98</statusCode>")) {
            return Mono.error(new RuntimeException("Error en SUNAT: " + soapResponse));
        }
        String base64Content = extractApplicationResponseWithRegex(soapResponse);
        byte[] originalBytes = convertFromBase64(base64Content);
        return Mono.just(new FileResponseDTO("Success", "File processed successfully", originalBytes, null));
    }

    private static byte[] convertFromBase64(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }

    private String extractApplicationResponseWithRegex(String soapResponse) {
        Pattern pattern = Pattern.compile("<content>(.*?)</content>");
        Matcher matcher = pattern.matcher(soapResponse);
        if (matcher.find()) {
            return matcher.group(1); // Retorna el contenido capturado
        }
        throw new RuntimeException("No se encontró <content> en la respuesta SOAP.");
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

            // 3. Buscar <statusCode>
            String statusCode = document.select("statusCode").text();
            if ("98".equals(statusCode)) {
                return "SUNAT aún está procesando el comprobante (código 98)";
            }

            return "";

        } catch (Exception e) {
            return "Error no especificado";
        }
    }
}

