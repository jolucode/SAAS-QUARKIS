package service.cloud.request.clientRequest.mongo.repo;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import service.cloud.request.clientRequest.mongo.model.DocumentPublication;

public interface IPublicarRepo extends ReactiveMongoRepository<DocumentPublication, String> {
}
