package service.cloud.request.clientRequest.notification.repo;

import io.smallrye.mutiny.Uni;
import service.cloud.request.clientRequest.notification.model.EmailSuppression;

public interface EmailSuppressionRepository {

    Uni<EmailSuppression> findByEmail(String email);

    Uni<EmailSuppression> save(EmailSuppression emailSuppression);
}
