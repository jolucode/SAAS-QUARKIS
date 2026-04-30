package service.cloud.request.clientRequest.mongo.repo;

import io.smallrye.mutiny.Uni;
import service.cloud.request.clientRequest.mongo.model.DocumentPublication;

public interface IPublicarRepo {
    Uni<DocumentPublication> save(DocumentPublication documentPublication);
}
