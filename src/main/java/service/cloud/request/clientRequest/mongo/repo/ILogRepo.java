package service.cloud.request.clientRequest.mongo.repo;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import service.cloud.request.clientRequest.mongo.model.Log;

public interface ILogRepo {
    Uni<Log> save(Log log);
    Uni<Log> findById(String id);
    Multi<Log> findAll();
    Uni<Void> deleteById(String id);
}
