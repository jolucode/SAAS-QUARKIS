package service.cloud.request.clientRequest.notification.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "email_suppressions")
public class EmailSuppression {

    @Id
    private String id;

    private String email;
    private SuppressionReason reason;
    private LocalDateTime addedAt;
    private String sourceRuc;
}
