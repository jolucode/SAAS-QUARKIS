package service.cloud.request.clientRequest.service.extractor;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface CloudInterface {

    Mono<ResponseEntity<Object>> proccessDocument(String ejemploString);

}
