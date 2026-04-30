package service.cloud.request.clientRequest.mongo.service.impl;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import service.cloud.request.clientRequest.mongo.model.DocumentPublication;
import service.cloud.request.clientRequest.mongo.repo.IPublicarRepo;
import service.cloud.request.clientRequest.mongo.service.IDocumentPublicationService;

@ApplicationScoped
@RequiredArgsConstructor
public class DocumentPublicationServiceImpl implements IDocumentPublicationService {

    private final IPublicarRepo publicarRepo;

    @Override
    public Uni<DocumentPublication> saveLogEntryToMongoDB(DocumentPublication publicar) {
        return publicarRepo.save(publicar);
    }

    @Override
    public Uni<DocumentPublication> save(DocumentPublication doc) {
        return publicarRepo.save(doc);
    }

    @Override
    public Uni<DocumentPublication> udpate(DocumentPublication doc, String id) {
        return Uni.createFrom().nullItem();
    }

    @Override
    public Multi<DocumentPublication> findAll() {
        return Multi.createFrom().empty();
    }

    @Override
    public Uni<DocumentPublication> findById(String id) {
        return Uni.createFrom().nullItem();
    }

    @Override
    public Uni<Boolean> delete(String id) {
        return Uni.createFrom().item(false);
    }
}
