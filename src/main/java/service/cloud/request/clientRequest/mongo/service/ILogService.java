package service.cloud.request.clientRequest.mongo.service;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import service.cloud.request.clientRequest.mongo.model.Log;

public interface ILogService {

    Uni<Log> saveLogEntryToMongoDB(Log logEntry);
    Uni<Log> save(Log log);
    Uni<Log> udpate(Log log, String id);
    Multi<Log> findAll();
    Uni<Log> findById(String id);
    Uni<Boolean> delete(String id);

}
