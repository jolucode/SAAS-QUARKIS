package service.cloud.request.clientRequest.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.notification.model.EmailSuppression;
import service.cloud.request.clientRequest.notification.model.SuppressionReason;
import service.cloud.request.clientRequest.notification.repo.EmailSuppressionRepository;

import java.time.LocalDateTime;

@Service
public class EmailSuppressionService {

    private static final Logger log = LoggerFactory.getLogger(EmailSuppressionService.class);

    @Autowired
    private EmailSuppressionRepository suppressionRepo;

    public Mono<Boolean> isSuppressed(String email) {
        return suppressionRepo.findByEmail(email.toLowerCase())
                .map(s -> {
                    log.warn("Email suprimido ({}): {}", s.getReason(), email);
                    return true;
                })
                .defaultIfEmpty(false);
    }

    public Mono<EmailSuppression> suppress(String email, SuppressionReason reason, String sourceRuc) {
        EmailSuppression suppression = new EmailSuppression();
        suppression.setEmail(email.toLowerCase());
        suppression.setReason(reason);
        suppression.setAddedAt(LocalDateTime.now());
        suppression.setSourceRuc(sourceRuc);
        return suppressionRepo.save(suppression)
                .doOnSuccess(s -> log.warn("Email agregado a supresión [{}]: {}", reason, email));
    }
}
