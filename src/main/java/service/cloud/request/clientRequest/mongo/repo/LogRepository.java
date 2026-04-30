package service.cloud.request.clientRequest.mongo.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import service.cloud.request.clientRequest.mongo.model.Log;

@Component
public interface LogRepository extends MongoRepository<Log, String> {
}
