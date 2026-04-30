package service.cloud.request.clientRequest.notification.repo;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import service.cloud.request.clientRequest.notification.model.EmailDelivery;

public interface EmailDeliveryRepository {

    Multi<EmailDelivery> findByJobId(String jobId);

    Uni<EmailDelivery> save(EmailDelivery emailDelivery);
}
