package service.cloud.request.clientRequest.mongo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.mongo.model.Log;
import service.cloud.request.clientRequest.mongo.repo.ILogRepo;
import service.cloud.request.clientRequest.mongo.service.ILogService;

@Service
@RequiredArgsConstructor
public class LogServiceImpl implements ILogService {

    private final ILogRepo repo;

    public Mono<Log> saveLogEntryToMongoDB(Log logEntry) {
        return repo.save(logEntry);
    }

    @Override
    public Mono<Log> save(Log log) {
        return repo.save(log);
    }

    @Override
    public Mono<Log> udpate(Log client, String id) {
        return repo.findById(id).flatMap(v -> repo.save(client));
    }

    @Override
    public Flux<Log> findAll() {
        return repo.findAll();
    }

    @Override
    public Mono<Log> findById(String id) {
        return repo.findById(id);
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return repo.findById(id)
                .hasElement()
                .flatMap(valor -> {
                    if (valor) {
                        return repo.deleteById(id).thenReturn(valor);
                    } else {
                        return Mono.just(!valor);
                    }
                });
    }
}
