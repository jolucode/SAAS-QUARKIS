package service.cloud.request.clientRequest.notification.config;

import lombok.Getter;
import lombok.Setter;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.ConfigProvider;

@Getter
@Setter
@ApplicationScoped
public class NotificationProperties {

    /** SENDGRID | SES | RESEND — cambia solo esta línea para cambiar de proveedor */
    private String provider = "SENDGRID";
    private String from = "Comprobantes <notificaciones@venturasoluciones.com>";
    private String portalBaseUrl = "http://localhost:3000";
    private String sendgridApiKey = "";

    private SendGrid sendgrid = new SendGrid();

    public String getProvider() {
        return ConfigProvider.getConfig().getOptionalValue("notification.provider", String.class).orElse(provider);
    }

    public String getFrom() {
        return ConfigProvider.getConfig().getOptionalValue("notification.from", String.class).orElse(from);
    }

    public String getPortalBaseUrl() {
        return ConfigProvider.getConfig().getOptionalValue("notification.portalBaseUrl", String.class).orElse(portalBaseUrl);
    }

    public SendGrid getSendgrid() {
        sendgridApiKey = ConfigProvider.getConfig().getOptionalValue("notification.sendgrid.apiKey", String.class).orElse("");
        sendgrid.setApiKey(sendgridApiKey);
        return sendgrid;
    }

    @Getter
    @Setter
    public static class SendGrid {
        private String apiKey;
    }
}
