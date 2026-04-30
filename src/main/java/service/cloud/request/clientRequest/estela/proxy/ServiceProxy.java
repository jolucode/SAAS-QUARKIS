package service.cloud.request.clientRequest.estela.proxy;

import jakarta.inject.Inject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import jakarta.enterprise.context.ApplicationScoped;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ApplicationScoped
public class ServiceProxy implements ServiceClient {

    private final WebClient webClient;

    @Inject
    public ServiceProxy(WebClient webClient) {
        this.webClient = webClient;
    }


    @Override
    public Mono<String> sendSoapRequest(String url, String soapRequest) {
        return webClient.post()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML_VALUE)
                .bodyValue(soapRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(body)))
                )
                .bodyToMono(String.class);
    }
}

