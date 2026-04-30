package service.cloud.request.clientRequest.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.regex.Pattern;

@ApplicationScoped
public class EmailValidatorService {

    private static final Logger log = LoggerFactory.getLogger(EmailValidatorService.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
    );

    public boolean isValid(String email) {
        if (email == null || email.isBlank()) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public String resolveRecipientEmail(String primaryEmail, String secondaryEmail) {
        if (isValid(primaryEmail)) return normalize(primaryEmail);
        if (isValid(secondaryEmail)) {
            log.info("Usando email secundario: {}", secondaryEmail);
            return normalize(secondaryEmail);
        }
        return null;
    }
}
