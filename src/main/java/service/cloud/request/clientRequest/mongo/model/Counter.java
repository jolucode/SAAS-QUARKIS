package service.cloud.request.clientRequest.mongo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Counter {
    @Id
    private String id; // ejemplo: RC-20510910517-20250802
    private long seq;

    // getters y setters
}