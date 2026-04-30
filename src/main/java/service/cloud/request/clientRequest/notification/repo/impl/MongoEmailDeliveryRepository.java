package service.cloud.request.clientRequest.notification.repo.impl;

import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import org.reactivestreams.FlowAdapters;
import service.cloud.request.clientRequest.notification.model.EmailDelivery;
import service.cloud.request.clientRequest.notification.repo.EmailDeliveryRepository;

@ApplicationScoped
public class MongoEmailDeliveryRepository implements EmailDeliveryRepository {
    private final MongoCollection<Document> collection;
    private final Gson gson = new Gson();

    public MongoEmailDeliveryRepository(MongoClient mongoClient) {
        this.collection = mongoClient.getDatabase("ventura2024").getCollection("email_deliveries");
    }

    public Multi<EmailDelivery> findByJobId(String jobId) {
        return Multi.createFrom().publisher(FlowAdapters.toFlowPublisher(collection.find(Filters.eq("jobId", jobId))))
                .map(d -> gson.fromJson(d.toJson(), EmailDelivery.class));
    }

    public Uni<EmailDelivery> save(EmailDelivery emailDelivery) {
        return Uni.createFrom().publisher(FlowAdapters.toFlowPublisher(collection.insertOne(Document.parse(gson.toJson(emailDelivery)))))
                .replaceWith(emailDelivery);
    }
}
