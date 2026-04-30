package service.cloud.request.clientRequest.mongo.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.mongo.model.Log;

public interface ILogService  {


  Mono<Log> saveLogEntryToMongoDB(Log logEntry);
  Mono<Log> save(Log Log);

  Mono<Log> udpate(Log Log, String id);

  Flux<Log> findAll();

  Mono<Log> findById(String id);

  Mono<Boolean> delete(String id);


}
