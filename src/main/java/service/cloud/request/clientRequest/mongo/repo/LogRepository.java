package service.cloud.request.clientRequest.mongo.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import service.cloud.request.clientRequest.mongo.model.Log;

@ApplicationScoped
public interface LogRepository extends MongoRepository<Log, String> {
}
