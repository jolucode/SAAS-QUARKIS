package service.cloud.request.clientRequest.mongo.repo;

import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.mongo.model.DocumentPublication;

public interface IPublicarRepo {
    Mono<DocumentPublication> save(DocumentPublication documentPublication);
}
