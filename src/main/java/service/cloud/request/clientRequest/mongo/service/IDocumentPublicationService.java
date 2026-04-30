package service.cloud.request.clientRequest.mongo.service;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import service.cloud.request.clientRequest.mongo.model.DocumentPublication;

public interface IDocumentPublicationService {

    Uni<DocumentPublication> saveLogEntryToMongoDB(DocumentPublication logEntry);
    Uni<DocumentPublication> save(DocumentPublication doc);
    Uni<DocumentPublication> udpate(DocumentPublication doc, String id);
    Multi<DocumentPublication> findAll();
    Uni<DocumentPublication> findById(String id);
    Uni<Boolean> delete(String id);

}
