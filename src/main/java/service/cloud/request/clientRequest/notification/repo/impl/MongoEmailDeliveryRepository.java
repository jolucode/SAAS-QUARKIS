package service.cloud.request.clientRequest.notification.repo.impl;

import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.notification.model.EmailDelivery;
import service.cloud.request.clientRequest.notification.repo.EmailDeliveryRepository;

@ApplicationScoped
public class MongoEmailDeliveryRepository implements EmailDeliveryRepository {
    private final MongoCollection<Document> collection;
    private final Gson gson = new Gson();

    public MongoEmailDeliveryRepository(MongoClient mongoClient) {
        this.collection = mongoClient.getDatabase("ventura2024").getCollection("email_deliveries");
    }

    public Flux<EmailDelivery> findByJobId(String jobId) {
        return Flux.from(collection.find(Filters.eq("jobId", jobId)))
                .map(d -> gson.fromJson(d.toJson(), EmailDelivery.class));
    }

    public Mono<EmailDelivery> save(EmailDelivery emailDelivery) {
        return Mono.from(collection.insertOne(Document.parse(gson.toJson(emailDelivery)))).thenReturn(emailDelivery);
    }
}
