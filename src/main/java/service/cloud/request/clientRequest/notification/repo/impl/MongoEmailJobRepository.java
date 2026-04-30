package service.cloud.request.clientRequest.notification.repo.impl;

import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.notification.model.EmailJob;
import service.cloud.request.clientRequest.notification.model.EmailJobStatus;
import service.cloud.request.clientRequest.notification.repo.EmailJobRepository;

import java.time.LocalDateTime;

@ApplicationScoped
public class MongoEmailJobRepository implements EmailJobRepository {
    private final MongoCollection<Document> collection;
    private final Gson gson = new Gson();

    public MongoEmailJobRepository(MongoClient mongoClient) {
        this.collection = mongoClient.getDatabase("ventura2024").getCollection("email_jobs");
    }

    public Flux<EmailJob> findByStatusAndNextRetryAtLessThanEqual(EmailJobStatus status, LocalDateTime now) {
        return Flux.from(collection.find(Filters.and(Filters.eq("status", status.name()), Filters.lte("nextRetryAt", now.toString())))
                        .sort(Sorts.ascending("nextRetryAt")))
                .map(d -> gson.fromJson(d.toJson(), EmailJob.class));
    }

    public Flux<EmailJob> findByStatusAndLockedAtLessThan(EmailJobStatus status, LocalDateTime lockedBefore) {
        return Flux.from(collection.find(Filters.and(Filters.eq("status", status.name()), Filters.lt("lockedAt", lockedBefore.toString()))))
                .map(d -> gson.fromJson(d.toJson(), EmailJob.class));
    }

    public Mono<EmailJob> findFirstByRucAndDocumentSeriesAndStatus(String ruc, String documentSeries, EmailJobStatus status) {
        return Mono.from(collection.find(Filters.and(
                                Filters.eq("ruc", ruc),
                                Filters.eq("documentSeries", documentSeries),
                                Filters.eq("status", status.name())))
                        .sort(Sorts.descending("createdAt")).first())
                .map(d -> gson.fromJson(d.toJson(), EmailJob.class));
    }

    public Mono<EmailJob> findById(String id) {
        return Mono.from(collection.find(Filters.eq("_id", id)).first())
                .map(d -> gson.fromJson(d.toJson(), EmailJob.class));
    }

    public Mono<EmailJob> save(EmailJob emailJob) {
        return Mono.from(collection.insertOne(Document.parse(gson.toJson(emailJob)))).thenReturn(emailJob);
    }
}
