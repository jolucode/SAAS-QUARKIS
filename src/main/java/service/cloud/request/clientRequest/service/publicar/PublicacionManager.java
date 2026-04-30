package service.cloud.request.clientRequest.service.publicar;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.model.Client;

import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
                .invoke(response -> {
                    logger.info("Documento publicado exitosamente [{}]", documentoInfo);
                    if (response != null && response.contains("correctamente")) {
                        logger.info("Publicación exitosa [{}]: {}", documentoInfo, response);
                    }
                })
                .onFailure().invoke(error -> logger.error("Error en la publicación para documento [{}]: {}", documentoInfo, error.getMessage()))
                .subscribe().with(v -> {}, e -> {});
    }

    public Uni<String> publicarDocumentoWs(String apiUrl, DocumentoPublicado documentoPublicado) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            String json = new com.google.gson.Gson().toJson(documentoPublicado);
            int maxRetries = 2;
            Exception lastError = null;
            for (int attempt = 0; attempt <= maxRetries; attempt++) {
                try {
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
                } catch (Exception e) {
                    lastError = e;
                    if (!isRetryableError(e) || attempt == maxRetries) throw e;
                    logger.warn("Reintentando publicación #{}: {}", attempt + 1, e.getMessage());
                    Thread.sleep(3000);
                }
            }
            throw lastError;
        }));
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
