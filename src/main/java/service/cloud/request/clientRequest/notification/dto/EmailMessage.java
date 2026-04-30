package service.cloud.request.clientRequest.notification.dto;

public record EmailMessage(
        String to,
        String toName,
        String replyTo,
        String subject,
        String htmlBody
) {}
