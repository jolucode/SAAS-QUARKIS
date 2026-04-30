package service.cloud.request.clientRequest.estela.proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ServiceProxy implements ServiceClient {

    private final WebClient webClient;

    @Autowired
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

