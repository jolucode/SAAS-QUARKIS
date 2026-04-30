package service.cloud.request.clientRequest.estela.proxy;

import jakarta.enterprise.context.ApplicationScoped;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@ApplicationScoped
public class ServiceProxy implements ServiceClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();


    @Override
    public Mono<String> sendSoapRequest(String url, String soapRequest) {
        return Mono.fromCallable(() -> {
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
        });
    }
}

