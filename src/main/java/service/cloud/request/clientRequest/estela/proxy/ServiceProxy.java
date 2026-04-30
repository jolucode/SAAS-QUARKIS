package service.cloud.request.clientRequest.estela.proxy;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@ApplicationScoped
public class ServiceProxy implements ServiceClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public Uni<String> sendSoapRequest(String url, String soapRequest) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "text/xml")
                    .POST(HttpRequest.BodyPublishers.ofString(soapRequest))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new RuntimeException(response.body());
            }
            return response.body();
        }));
    }
}
