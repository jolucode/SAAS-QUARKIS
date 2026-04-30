package service.cloud.request.clientRequest.mongo.service.impl;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import service.cloud.request.clientRequest.mongo.model.Log;
import service.cloud.request.clientRequest.mongo.repo.ILogRepo;
import service.cloud.request.clientRequest.mongo.service.ILogService;

@ApplicationScoped
@RequiredArgsConstructor
public class LogServiceImpl implements ILogService {

    private final ILogRepo repo;

    public Uni<Log> saveLogEntryToMongoDB(Log logEntry) {
        return repo.save(logEntry);
    }

    @Override
    public Uni<Log> save(Log log) {
        return repo.save(log);
    }

    @Override
    public Uni<Log> udpate(Log client, String id) {
        return repo.findById(id).chain(v -> repo.save(client));
    }

    @Override
    public Multi<Log> findAll() {
        return repo.findAll();
    }

    @Override
    public Uni<Log> findById(String id) {
        return repo.findById(id);
    }

    @Override
    public Uni<Boolean> delete(String id) {
        return repo.findById(id)
                .onItem().ifNotNull().transformToUni(v -> repo.deleteById(id).replaceWith(true))
                .onItem().ifNull().continueWith(false);
    }
}
