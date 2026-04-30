package service.cloud.request.clientRequest.notification.repo;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import service.cloud.request.clientRequest.notification.model.EmailJob;
import service.cloud.request.clientRequest.notification.model.EmailJobStatus;

import java.time.LocalDateTime;

public interface EmailJobRepository {

    Multi<EmailJob> findByStatusAndNextRetryAtLessThanEqual(EmailJobStatus status, LocalDateTime now);

    Multi<EmailJob> findByStatusAndLockedAtLessThan(EmailJobStatus status, LocalDateTime lockedBefore);

    Uni<EmailJob> findFirstByRucAndDocumentSeriesAndStatus(String ruc, String documentSeries, EmailJobStatus status);

    Uni<EmailJob> findById(String id);

    Uni<EmailJob> save(EmailJob emailJob);
}
