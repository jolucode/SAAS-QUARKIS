package service.cloud.request.clientRequest.service.extractor;

import jakarta.ws.rs.core.Response;
import reactor.core.publisher.Mono;

public interface CloudInterface {

    Mono<Response> proccessDocument(String ejemploString);

}
