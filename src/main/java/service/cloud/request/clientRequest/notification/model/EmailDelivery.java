package service.cloud.request.clientRequest.notification.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDelivery {

    private String id;

    private String jobId;
    private String messageId;
    private EmailDeliveryStatus status;
    private LocalDateTime sentAt;
    private String error;
    private String provider;
}
