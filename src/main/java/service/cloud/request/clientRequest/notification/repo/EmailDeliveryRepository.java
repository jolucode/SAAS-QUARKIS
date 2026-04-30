package service.cloud.request.clientRequest.notification.repo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.notification.model.EmailDelivery;

public interface EmailDeliveryRepository {

    Flux<EmailDelivery> findByJobId(String jobId);
    Mono<EmailDelivery> save(EmailDelivery emailDelivery);
}
