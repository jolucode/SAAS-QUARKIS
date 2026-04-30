package service.cloud.request.clientRequest.estela.proxy;

import reactor.core.publisher.Mono;

public interface ServiceClient {
    Mono<String> sendSoapRequest(String url, String soapRequest);
}
