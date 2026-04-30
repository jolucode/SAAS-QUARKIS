package service.cloud.request.clientRequest.notification.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.cloud.request.clientRequest.notification.config.NotificationProperties;
import service.cloud.request.clientRequest.notification.dto.EmailMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
@Named("sendGridProvider")
public class SendGridProvider implements EmailProvider {

    private static final Logger log = LoggerFactory.getLogger(SendGridProvider.class);
    private static final String SENDGRID_URL = "https://api.sendgrid.com/v3/mail/send";
    private static final Pattern FROM_PATTERN = Pattern.compile("^(.+?)\\s*<([^>]+)>$");

    @Inject
    private NotificationProperties props;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public String send(EmailMessage message) throws Exception {
        String apiKey = props.getSendgrid().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("SendGrid API key no configurado. Revisa notification.sendgrid.apiKey");
        }

        String body = buildRequestBody(message);
        AtomicReference<String> messageId = new AtomicReference<>("sg-" + System.currentTimeMillis());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SENDGRID_URL))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            String msg = extractSendGridError(response.body());
            throw new RuntimeException("SendGrid error [" + response.statusCode() + "]: " + msg);
        }
        String header = response.headers().firstValue("X-Message-Id").orElse(null);
        if (header != null) {
            messageId.set(header);
        }
        log.info("Email enviado via SendGrid a {} | X-Message-Id: {}", message.to(), messageId.get());

        return messageId.get();
    }

    @Override
    public String providerName() {
        return "SENDGRID";
    }

    private String buildRequestBody(EmailMessage message) {
        ParsedAddress from = parseAddress(props.getFrom());

        JsonObject fromObj = new JsonObject();
        fromObj.addProperty("email", from.email());
        if (!from.name().isBlank()) fromObj.addProperty("name", from.name());

        JsonObject toObj = new JsonObject();
        toObj.addProperty("email", message.to());
        if (message.toName() != null && !message.toName().isBlank()) {
            toObj.addProperty("name", message.toName());
        }

        JsonArray toArray = new JsonArray();
        toArray.add(toObj);

        JsonObject personalization = new JsonObject();
        personalization.add("to", toArray);

        JsonArray personalizations = new JsonArray();
        personalizations.add(personalization);

        JsonObject content = new JsonObject();
        content.addProperty("type", "text/html");
        content.addProperty("value", message.htmlBody());

        JsonArray contentArray = new JsonArray();
        contentArray.add(content);

        JsonObject body = new JsonObject();
        body.add("personalizations", personalizations);
        body.add("from", fromObj);
        body.addProperty("subject", message.subject());
        body.add("content", contentArray);

        if (message.replyTo() != null && !message.replyTo().isBlank()) {
            ParsedAddress replyTo = parseAddress(message.replyTo());
            JsonObject replyToObj = new JsonObject();
            replyToObj.addProperty("email", replyTo.email());
            body.add("reply_to", replyToObj);
        }

        return gson.toJson(body);
    }

    private String extractSendGridError(String responseBody) {
        try {
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            if (json.has("errors")) {
                JsonArray errors = json.getAsJsonArray("errors");
                if (!errors.isEmpty()) {
                    return errors.get(0).getAsJsonObject().get("message").getAsString();
                }
            }
        } catch (Exception ignored) {}
        return responseBody;
    }

    private ParsedAddress parseAddress(String address) {
        if (address == null) return new ParsedAddress("", "");
        Matcher m = FROM_PATTERN.matcher(address.trim());
        if (m.matches()) return new ParsedAddress(m.group(2).trim(), m.group(1).trim());
        return new ParsedAddress(address.trim(), "");
    }

    private record ParsedAddress(String email, String name) {}
}
