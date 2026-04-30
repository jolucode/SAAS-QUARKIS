package service.cloud.request.clientRequest.notification.repo.impl;

import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import org.reactivestreams.FlowAdapters;
import service.cloud.request.clientRequest.notification.model.EmailSuppression;
import service.cloud.request.clientRequest.notification.repo.EmailSuppressionRepository;

@ApplicationScoped
public class MongoEmailSuppressionRepository implements EmailSuppressionRepository {
    private final MongoCollection<Document> collection;
    private final Gson gson = new Gson();

    public MongoEmailSuppressionRepository(MongoClient mongoClient) {
        this.collection = mongoClient.getDatabase("ventura2024").getCollection("email_suppressions");
    }

    public Uni<EmailSuppression> findByEmail(String email) {
        return Uni.createFrom().publisher(FlowAdapters.toFlowPublisher(collection.find(Filters.eq("email", email)).first()))
                .map(d -> gson.fromJson(d.toJson(), EmailSuppression.class));
    }

    public Uni<EmailSuppression> save(EmailSuppression emailSuppression) {
        return Uni.createFrom().publisher(FlowAdapters.toFlowPublisher(collection.insertOne(Document.parse(gson.toJson(emailSuppression)))))
                .replaceWith(emailSuppression);
    }
}
