package service.cloud.request.clientRequest.notification.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.cloud.request.clientRequest.notification.model.SuppressionReason;
import service.cloud.request.clientRequest.notification.service.EmailSuppressionService;
import service.cloud.request.clientRequest.notification.service.NotificationManager;

@Path("/api/notification")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @Inject
    private NotificationManager notificationManager;

    @Inject
    private EmailSuppressionService suppressionService;

    private final Gson gson = new Gson();

    @POST
    @Path("/resend/{jobId}")
    public Response resend(@PathParam("jobId") String jobId) {
        notificationManager.resend(jobId);
        return Response.ok("Reenvio programado para job: " + jobId).build();
    }

    @POST
    @Path("/webhooks/ses")
    public Uni<Response> sesWebhook(String payload) {
        return Uni.createFrom().item(() -> {
            JsonObject sns = gson.fromJson(payload, JsonObject.class);
            String type = sns.has("Type") ? sns.get("Type").getAsString() : "";

            if ("SubscriptionConfirmation".equals(type)) {
                log.info("SNS SubscriptionConfirmation recibido — confirme manualmente la URL: {}",
                        sns.has("SubscribeURL") ? sns.get("SubscribeURL").getAsString() : "N/A");
                return Response.ok("OK").build();
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

            return Response.ok("OK").build();
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
