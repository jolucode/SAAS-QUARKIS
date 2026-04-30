package service.cloud.request.clientRequest.notification.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailSuppression {

    private String id;

    private String email;
    private SuppressionReason reason;
    private LocalDateTime addedAt;
    private String sourceRuc;
}
