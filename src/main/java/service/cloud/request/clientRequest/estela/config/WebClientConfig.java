package service.cloud.request.clientRequest.estela.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.springframework.web.reactive.function.client.WebClient;

@ApplicationScoped
public class WebClientConfig {

    @Produces
    public WebClient webClient() {
        return WebClient.builder()
                .defaultHeader("Content-Type", "text/xml")
                .build();
    }
}
