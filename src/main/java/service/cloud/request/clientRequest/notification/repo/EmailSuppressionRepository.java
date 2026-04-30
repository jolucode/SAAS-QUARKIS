package service.cloud.request.clientRequest.notification.repo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.notification.model.EmailSuppression;

public interface EmailSuppressionRepository extends ReactiveMongoRepository<EmailSuppression, String> {

    Mono<EmailSuppression> findByEmail(String email);
}
