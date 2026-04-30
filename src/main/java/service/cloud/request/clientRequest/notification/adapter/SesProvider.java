package service.cloud.request.clientRequest.notification.adapter;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import service.cloud.request.clientRequest.notification.config.NotificationProperties;
import service.cloud.request.clientRequest.notification.dto.EmailMessage;

/**
 * Proveedor SES via SMTP relay.
 * Activo cuando notification.provider=SES en configProcesador.yml.
 * Requiere configurar spring.mail.* con credenciales de SES.
 */
@Component
@ConditionalOnProperty(name = "notification.provider", havingValue = "SES")
public class SesProvider implements EmailProvider {

    private static final Logger log = LoggerFactory.getLogger(SesProvider.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private NotificationProperties props;

    @Override
    public String send(EmailMessage message) throws Exception {
        if (mailSender == null) {
            throw new IllegalStateException("SES SMTP no configurado. Revisa spring.mail.* en configProcesador.yml");
        }

        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

        helper.setFrom(props.getFrom());
        helper.setTo(message.to());
        helper.setSubject(message.subject());
        helper.setText(message.htmlBody(), true);

        if (message.replyTo() != null && !message.replyTo().isBlank()) {
            helper.setReplyTo(message.replyTo());
        }

        mime.saveChanges();
        String messageId = mime.getMessageID();
        mailSender.send(mime);

        log.info("Email enviado via SES SMTP a {} | messageId: {}", message.to(), messageId);
        return messageId;
    }

    @Override
    public String providerName() {
        return "SES_SMTP";
    }
}
