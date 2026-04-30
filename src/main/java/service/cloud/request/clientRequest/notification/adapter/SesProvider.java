package service.cloud.request.clientRequest.notification.adapter;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import service.cloud.request.clientRequest.notification.config.NotificationProperties;
import service.cloud.request.clientRequest.notification.dto.EmailMessage;

import java.util.Properties;

/**
 * Proveedor SES via SMTP relay.
 * Activo cuando notification.provider=SES en configProcesador.yml.
 * Requiere configurar quarkus.mailer.* con credenciales de SES.
 */
@ApplicationScoped
@Named("sesProvider")
public class SesProvider implements EmailProvider {

    private static final Logger log = LoggerFactory.getLogger(SesProvider.class);

    @Inject
    private NotificationProperties props;

    @Override
    public String send(EmailMessage message) throws Exception {
        String smtpHost = getConfigValue("quarkus.mailer.host", "notification.ses.smtp.host",
                "email-smtp.us-east-1.amazonaws.com");
        String smtpPort = getConfigValue("quarkus.mailer.port", "notification.ses.smtp.port", "587");
        String smtpUser = getConfigValue("quarkus.mailer.username", "notification.ses.smtp.username", "");
        String smtpPass = getConfigValue("quarkus.mailer.password", "notification.ses.smtp.password", "");
        String smtpAuth = getConfigValue("notification.ses.smtp.auth", "quarkus.mailer.auth", "true");
        String smtpStartTls = getConfigValue("notification.ses.smtp.starttls.enable",
                "quarkus.mailer.start-tls", "true");

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", smtpAuth);
        properties.put("mail.smtp.starttls.enable", smtpStartTls);
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);

        Session session = Session.getInstance(properties);
        MimeMessage mime = new MimeMessage(session);
        mime.setFrom(props.getFrom());
        mime.setRecipients(jakarta.mail.Message.RecipientType.TO, message.to());
        mime.setSubject(message.subject(), "UTF-8");
        mime.setContent(message.htmlBody(), "text/html; charset=UTF-8");

        if (message.replyTo() != null && !message.replyTo().isBlank()) {
            mime.setReplyTo(new jakarta.mail.Address[]{new jakarta.mail.internet.InternetAddress(message.replyTo())});
        }

        mime.saveChanges();
        String messageId = mime.getMessageID();
        Transport transport = session.getTransport("smtp");
        transport.connect(smtpHost, smtpUser, smtpPass);
        transport.sendMessage(mime, mime.getAllRecipients());
        transport.close();

        log.info("Email enviado via SES SMTP a {} | messageId: {}", message.to(), messageId);
        return messageId;
    }

    private String getConfigValue(String primaryKey, String fallbackKey, String defaultValue) {
        return ConfigProvider.getConfig().getOptionalValue(primaryKey, String.class)
                .orElse(ConfigProvider.getConfig().getOptionalValue(fallbackKey, String.class)
                        .orElse(defaultValue));
    }

    @Override
    public String providerName() {
        return "SES_SMTP";
    }
}
