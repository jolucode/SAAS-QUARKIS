package service.cloud.request.clientRequest.service.publicar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.model.Client;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class PublicacionManager {

    Logger logger = LoggerFactory.getLogger(PublicacionManager.class);

    @Inject
    ClientProperties clientProperties;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void publicarDocumento(TransacctionDTO transacctionDTO, String feId, TransaccionRespuesta transaccionRespuesta) {
        String documentoInfo = "";
        try {
            if (transacctionDTO == null || transaccionRespuesta == null) {
                logger.warn("Datos insuficientes para publicar documento");
                return;
            }
            documentoInfo = String.format("RUC: %s, Tipo: %s, Serie-Correlativo: %s",
                    transacctionDTO.getDocIdentidad_Nro(),
                    transacctionDTO.getDOC_Codigo(),
                    transacctionDTO.getDOC_Id());

            Client client = clientProperties.listaClientesOf(transacctionDTO.getDocIdentidad_Nro());
            DocumentoPublicado documentoPublicado = new DocumentoPublicado(client, transacctionDTO, transaccionRespuesta);
            //realizarPublicacion(client, documentoPublicado, documentoInfo);

            String url = client.getWsLocation();
            if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                realizarPublicacion(client, documentoPublicado, documentoInfo);
            } else {
                logger.info("No se publicó el documento. URL inválida: {} | {}", url, documentoInfo);
            }

        } catch (Exception e) {
            logger.error("Error al intentar publicar el documento [{}]: {}", documentoInfo, e.getMessage());
        }
    }

    private void realizarPublicacion(Client client, DocumentoPublicado documentoPublicado, String documentoInfo) {
        publicarDocumentoWs(client.getWsLocation(), documentoPublicado)
                .doOnSuccess(response -> {
                    logger.info("Documento publicado exitosamente [{}]", documentoInfo);
                    if (response != null && response.contains("correctamente")) {
                        logger.info("Publicación exitosa [{}]: {}", documentoInfo, response);
                        logger.info("Publicado en: {} para documento [{}]", client.getWsLocation(), documentoInfo);
                    }
                })
                .doOnError(error -> logError("Error en la publicación para documento [" + documentoInfo + "]", error))
                .subscribe();
    }


    private void logError(String message, Throwable error) {
        logger.error(message + ": " + error.getMessage());
    }

//    public Mono<String> publicarDocumentoWs(String apiUrl, DocumentoPublicado documentoPublicado) {
//
//        WebClient webClient = WebClient.create(apiUrl);
//        return webClient.post()
//                .uri("/api/publicar")
//                .body(BodyInserters.fromValue(documentoPublicado))
//                .retrieve()
//                .bodyToMono(String.class);
//    }

    public Mono<String> publicarDocumentoWs(String apiUrl, DocumentoPublicado documentoPublicado) {
        return Mono.fromCallable(() -> {
                    String json = new com.google.gson.Gson().toJson(documentoPublicado);
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(apiUrl + "/api/publicar"))
                            .timeout(Duration.ofSeconds(24))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() >= 400) {
                        throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
                    }
                    return response.body();
                })
                .timeout(Duration.ofSeconds(24))
                .retryWhen(
                        Retry.backoff(2, Duration.ofSeconds(3))
                                .filter(this::isRetryableError)
                                .doBeforeRetry(retrySignal -> logger.warn(
                                        "Reintentando publicación {}. Intento #{} | Error: {}",
                                        documentoPublicado.getNumSerie(),
                                        retrySignal.totalRetries() + 1,
                                        retrySignal.failure().getMessage()
                                ))
                );
    }

    private boolean isRetryableError(Throwable throwable) {
        String message = throwable.getMessage() == null ? "" : throwable.getMessage().toLowerCase();

        if (message.contains("http 408") || message.contains("http 429") || message.contains("http 500")
                || message.contains("http 502") || message.contains("http 503") || message.contains("http 504")) {
            return true;
        }

        return throwable instanceof TimeoutException
                || throwable.getCause() instanceof UnknownHostException
                || throwable.getCause() instanceof ConnectException
                || message.contains("connection reset")
                || message.contains("prematurely closed");
    }

}
