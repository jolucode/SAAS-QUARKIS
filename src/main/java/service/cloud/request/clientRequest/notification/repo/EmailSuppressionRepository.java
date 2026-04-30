package service.cloud.request.clientRequest.notification.repo;

import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.notification.model.EmailSuppression;

public interface EmailSuppressionRepository {

    Mono<EmailSuppression> findByEmail(String email);
    Mono<EmailSuppression> save(EmailSuppression emailSuppression);
}
