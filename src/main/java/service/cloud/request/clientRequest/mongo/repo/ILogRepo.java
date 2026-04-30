package service.cloud.request.clientRequest.mongo.repo;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import service.cloud.request.clientRequest.mongo.model.Log;

public interface ILogRepo extends ReactiveMongoRepository<Log, String> {
}
