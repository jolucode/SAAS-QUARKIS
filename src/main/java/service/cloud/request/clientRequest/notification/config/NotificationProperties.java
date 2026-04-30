package service.cloud.request.clientRequest.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    /** SENDGRID | SES | RESEND — cambia solo esta línea para cambiar de proveedor */
    private String provider = "SENDGRID";

    private String from = "Comprobantes <notificaciones@venturasoluciones.com>";
    private String portalBaseUrl = "http://localhost:3000";

    private SendGrid sendgrid = new SendGrid();

    @Getter
    @Setter
    public static class SendGrid {
        private String apiKey = "";
    }
}
