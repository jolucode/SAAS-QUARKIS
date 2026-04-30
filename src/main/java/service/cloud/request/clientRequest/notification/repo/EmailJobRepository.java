package service.cloud.request.clientRequest.notification.repo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.notification.model.EmailJob;
import service.cloud.request.clientRequest.notification.model.EmailJobStatus;

import java.time.LocalDateTime;

public interface EmailJobRepository extends ReactiveMongoRepository<EmailJob, String> {

    Flux<EmailJob> findByStatusAndNextRetryAtLessThanEqual(EmailJobStatus status, LocalDateTime now);

    Flux<EmailJob> findByStatusAndLockedAtLessThan(EmailJobStatus status, LocalDateTime lockedBefore);

    Mono<EmailJob> findFirstByRucAndDocumentSeriesAndStatus(String ruc, String documentSeries, EmailJobStatus status);
}
