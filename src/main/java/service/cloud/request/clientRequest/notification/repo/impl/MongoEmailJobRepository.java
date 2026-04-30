package service.cloud.request.clientRequest.notification.repo.impl;

import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import org.reactivestreams.FlowAdapters;
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

    public Multi<EmailJob> findByStatusAndNextRetryAtLessThanEqual(EmailJobStatus status, LocalDateTime now) {
        return Multi.createFrom().publisher(FlowAdapters.toFlowPublisher(
                        collection.find(Filters.and(
                                        Filters.eq("status", status.name()),
                                        Filters.lte("nextRetryAt", now.toString())))
                                .sort(Sorts.ascending("nextRetryAt"))))
                .map(d -> gson.fromJson(d.toJson(), EmailJob.class));
    }

    public Multi<EmailJob> findByStatusAndLockedAtLessThan(EmailJobStatus status, LocalDateTime lockedBefore) {
        return Multi.createFrom().publisher(FlowAdapters.toFlowPublisher(
                        collection.find(Filters.and(
                                Filters.eq("status", status.name()),
                                Filters.lt("lockedAt", lockedBefore.toString())))))
                .map(d -> gson.fromJson(d.toJson(), EmailJob.class));
    }

    public Uni<EmailJob> findFirstByRucAndDocumentSeriesAndStatus(String ruc, String documentSeries, EmailJobStatus status) {
        return Uni.createFrom().publisher(FlowAdapters.toFlowPublisher(
                        collection.find(Filters.and(
                                        Filters.eq("ruc", ruc),
                                        Filters.eq("documentSeries", documentSeries),
                                        Filters.eq("status", status.name())))
                                .sort(Sorts.descending("createdAt")).first()))
                .map(d -> gson.fromJson(d.toJson(), EmailJob.class));
    }

    public Uni<EmailJob> findById(String id) {
        return Uni.createFrom().publisher(FlowAdapters.toFlowPublisher(collection.find(Filters.eq("_id", id)).first()))
                .map(d -> gson.fromJson(d.toJson(), EmailJob.class));
    }

    public Uni<EmailJob> save(EmailJob emailJob) {
        return Uni.createFrom().publisher(FlowAdapters.toFlowPublisher(collection.insertOne(Document.parse(gson.toJson(emailJob)))))
                .replaceWith(emailJob);
    }
}
