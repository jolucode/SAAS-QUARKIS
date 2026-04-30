package service.cloud.request.clientRequest.notification.repo.impl;

import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.notification.model.EmailSuppression;
import service.cloud.request.clientRequest.notification.repo.EmailSuppressionRepository;

@ApplicationScoped
public class MongoEmailSuppressionRepository implements EmailSuppressionRepository {
    private final MongoCollection<Document> collection;
    private final Gson gson = new Gson();

    public MongoEmailSuppressionRepository(MongoClient mongoClient) {
        this.collection = mongoClient.getDatabase("ventura2024").getCollection("email_suppressions");
    }

    public Mono<EmailSuppression> findByEmail(String email) {
        return Mono.from(collection.find(Filters.eq("email", email)).first())
                .map(d -> gson.fromJson(d.toJson(), EmailSuppression.class));
    }

    public Mono<EmailSuppression> save(EmailSuppression emailSuppression) {
        return Mono.from(collection.insertOne(Document.parse(gson.toJson(emailSuppression)))).thenReturn(emailSuppression);
    }
}
