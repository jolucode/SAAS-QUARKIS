package service.cloud.request.clientRequest.mongo.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.mongo.model.DocumentPublication;

public interface IDocumentPublicationService {


  Mono<DocumentPublication> saveLogEntryToMongoDB(DocumentPublication logEntry);
  Mono<DocumentPublication> save(DocumentPublication Log);

  Mono<DocumentPublication> udpate(DocumentPublication Log, String id);

  Flux<DocumentPublication> findAll();

  Mono<DocumentPublication> findById(String id);

  Mono<Boolean> delete(String id);


}
