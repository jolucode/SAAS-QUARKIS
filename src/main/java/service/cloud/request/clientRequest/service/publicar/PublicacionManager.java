package service.cloud.request.clientRequest.service.publicar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.inject.Inject;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import jakarta.enterprise.context.ApplicationScoped;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.model.Client;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class PublicacionManager {

    Logger logger = LoggerFactory.getLogger(PublicacionManager.class);

    @Inject
    ClientProperties clientProperties;

    private final WebClient webClient = WebClient.builder().build();

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
        return webClient.post()
                .uri(apiUrl + "/api/publicar")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(documentoPublicado)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.createException().flatMap(Mono::error)
                )
                .bodyToMono(String.class)
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

        if (throwable instanceof WebClientResponseException ex) {
            int status = ex.getStatusCode().value();
            return status == 408
                    || status == 429
                    || status == 500
                    || status == 502
                    || status == 503
                    || status == 504;
        }

        if (throwable instanceof WebClientRequestException ex) {
            Throwable cause = ex.getCause();
            String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();

            return cause instanceof UnknownHostException
                    || cause instanceof ConnectException
                    || cause instanceof TimeoutException
                    || cause instanceof io.netty.handler.timeout.TimeoutException
                    || message.contains("connection reset")
                    || message.contains("prematurely closed");
        }

        String message = throwable.getMessage() == null ? "" : throwable.getMessage().toLowerCase();

        return throwable instanceof TimeoutException
                || throwable instanceof io.netty.handler.timeout.TimeoutException
                || message.contains("connection reset")
                || message.contains("prematurely closed");
    }

}