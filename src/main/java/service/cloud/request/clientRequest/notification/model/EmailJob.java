package service.cloud.request.clientRequest.notification.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "email_jobs")
public class EmailJob {

    @Id
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
