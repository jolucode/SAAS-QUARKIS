package service.cloud.request.clientRequest.mongo.repo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.mongo.model.Log;

public interface ILogRepo {
    Mono<Log> save(Log log);
    Mono<Log> findById(String id);
    Flux<Log> findAll();
    Mono<Void> deleteById(String id);
}
