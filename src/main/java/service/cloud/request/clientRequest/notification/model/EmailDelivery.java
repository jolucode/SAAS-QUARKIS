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
@Document(collection = "email_deliveries")
public class EmailDelivery {

    @Id
    private String id;

    private String jobId;
    private String messageId;
    private EmailDeliveryStatus status;
    private LocalDateTime sentAt;
    private String error;
    private String provider;
}
