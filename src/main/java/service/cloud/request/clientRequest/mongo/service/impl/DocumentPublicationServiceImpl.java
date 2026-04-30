package service.cloud.request.clientRequest.mongo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.mongo.model.DocumentPublication;
import service.cloud.request.clientRequest.mongo.repo.IPublicarRepo;
import service.cloud.request.clientRequest.mongo.service.IDocumentPublicationService;

@Service
@RequiredArgsConstructor
public class DocumentPublicationServiceImpl implements IDocumentPublicationService {

    private final IPublicarRepo publicarRepo;


    @Override
    public Mono<DocumentPublication> saveLogEntryToMongoDB(DocumentPublication publicar) {
        return publicarRepo.save(publicar);
    }

    @Override
    public Mono<DocumentPublication> save(DocumentPublication Log) {
        return null;
    }

    @Override
    public Mono<DocumentPublication> udpate(DocumentPublication Log, String id) {
        return null;
    }

    @Override
    public Flux<DocumentPublication> findAll() {
        return null;
    }

    @Override
    public Mono<DocumentPublication> findById(String id) {
        return null;
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return null;
    }
}
