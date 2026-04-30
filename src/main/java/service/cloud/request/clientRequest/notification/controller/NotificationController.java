package service.cloud.request.clientRequest.notification.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.notification.model.SuppressionReason;
import service.cloud.request.clientRequest.notification.service.EmailSuppressionService;
import service.cloud.request.clientRequest.notification.service.NotificationManager;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationManager notificationManager;

    @Autowired
    private EmailSuppressionService suppressionService;

    private final Gson gson = new Gson();

    @PostMapping("/resend/{jobId}")
    public ResponseEntity<String> resend(@PathVariable String jobId) {
        notificationManager.resend(jobId);
        return ResponseEntity.ok("Reenvío programado para job: " + jobId);
    }

    @PostMapping("/webhooks/ses")
    public Mono<ResponseEntity<String>> sesWebhook(@RequestBody String payload) {
        return Mono.fromCallable(() -> {
            JsonObject sns = gson.fromJson(payload, JsonObject.class);
            String type = sns.has("Type") ? sns.get("Type").getAsString() : "";

            if ("SubscriptionConfirmation".equals(type)) {
                log.info("SNS SubscriptionConfirmation recibido — confirme manualmente la URL: {}",
                        sns.has("SubscribeURL") ? sns.get("SubscribeURL").getAsString() : "N/A");
                return ResponseEntity.ok("OK");
            }

            if ("Notification".equals(type) && sns.has("Message")) {
                JsonObject message = gson.fromJson(sns.get("Message").getAsString(), JsonObject.class);
                String notifType = message.has("notificationType")
                        ? message.get("notificationType").getAsString() : "";

                if ("Bounce".equals(notifType)) {
                    handleBounce(message);
                } else if ("Complaint".equals(notifType)) {
                    handleComplaint(message);
                }
            }

            return ResponseEntity.ok("OK");
        });
    }

    private void handleBounce(JsonObject message) {
        if (!message.has("bounce")) return;
        JsonObject bounce = message.getAsJsonObject("bounce");
        String bounceType = bounce.has("bounceType") ? bounce.get("bounceType").getAsString() : "";

        if (!"Permanent".equals(bounceType)) return;

        if (bounce.has("bouncedRecipients")) {
            bounce.getAsJsonArray("bouncedRecipients").forEach(el -> {
                String email = el.getAsJsonObject().has("emailAddress")
                        ? el.getAsJsonObject().get("emailAddress").getAsString() : null;
                if (email != null) {
                    suppressionService.suppress(email, SuppressionReason.HARD_BOUNCE, "SES-WEBHOOK")
                            .subscribe();
                }
            });
        }
    }

    private void handleComplaint(JsonObject message) {
        if (!message.has("complaint")) return;
        JsonObject complaint = message.getAsJsonObject("complaint");

        if (complaint.has("complainedRecipients")) {
            complaint.getAsJsonArray("complainedRecipients").forEach(el -> {
                String email = el.getAsJsonObject().has("emailAddress")
                        ? el.getAsJsonObject().get("emailAddress").getAsString() : null;
                if (email != null) {
                    log.warn("COMPLAINT recibido de {}", email);
                    suppressionService.suppress(email, SuppressionReason.COMPLAINT, "SES-WEBHOOK")
                            .subscribe();
                }
            });
        }
    }
}
