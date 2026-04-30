package service.cloud.request.clientRequest.notification.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailJob {

    private String id;

    private String ruc;
    private String emisorName;
    private String recipientEmail;
    private String recipientName;
    private String documentSeries;
    private String documentType;
    private BigDecimal amount;
    private String currency;
    private String portalUrl;

    private EmailJobStatus status;
    private int attempts;
    private LocalDateTime createdAt;
    private LocalDateTime nextRetryAt;
    private LocalDateTime lockedAt;
    private String lastError;
}
