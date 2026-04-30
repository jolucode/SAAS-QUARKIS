package service.cloud.request.clientRequest.notification.repo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import service.cloud.request.clientRequest.notification.model.EmailDelivery;

public interface EmailDeliveryRepository extends ReactiveMongoRepository<EmailDelivery, String> {

    Flux<EmailDelivery> findByJobId(String jobId);
}
