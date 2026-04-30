package service.cloud.request.clientRequest.notification.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.cloud.request.clientRequest.notification.model.EmailSuppression;
import service.cloud.request.clientRequest.notification.model.SuppressionReason;
import service.cloud.request.clientRequest.notification.repo.EmailSuppressionRepository;

import java.time.LocalDateTime;

@ApplicationScoped
public class EmailSuppressionService {

    private static final Logger log = LoggerFactory.getLogger(EmailSuppressionService.class);

    @Inject
    private EmailSuppressionRepository suppressionRepo;

    public Uni<Boolean> isSuppressed(String email) {
        return suppressionRepo.findByEmail(email.toLowerCase())
                .map(s -> {
                    if (s != null) {
                        log.warn("Email suprimido ({}): {}", s.getReason(), email);
                        return true;
                    }
                    return false;
                })
                .onItem().ifNull().continueWith(false);
    }

    public Uni<EmailSuppression> suppress(String email, SuppressionReason reason, String sourceRuc) {
        EmailSuppression suppression = new EmailSuppression();
        suppression.setEmail(email.toLowerCase());
        suppression.setReason(reason);
        suppression.setAddedAt(LocalDateTime.now());
        suppression.setSourceRuc(sourceRuc);
        return suppressionRepo.save(suppression)
                .invoke(s -> log.warn("Email agregado a supresión [{}]: {}", reason, email));
    }
}
