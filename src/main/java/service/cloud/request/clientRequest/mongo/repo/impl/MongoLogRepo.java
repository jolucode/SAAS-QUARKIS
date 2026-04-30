package service.cloud.request.clientRequest.mongo.repo.impl;

import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import org.reactivestreams.FlowAdapters;
import service.cloud.request.clientRequest.mongo.model.Log;
import service.cloud.request.clientRequest.mongo.repo.ILogRepo;

@ApplicationScoped
public class MongoLogRepo implements ILogRepo {
    private final MongoCollection<Document> collection;
    private final Gson gson = new Gson();

    public MongoLogRepo(MongoClient mongoClient) {
        this.collection = mongoClient.getDatabase("ventura2024").getCollection("logsPath");
    }

    public Uni<Log> save(Log log) {
        return Uni.createFrom().publisher(FlowAdapters.toFlowPublisher(collection.insertOne(Document.parse(gson.toJson(log)))))
                .replaceWith(log);
    }

    public Uni<Log> findById(String id) {
        return Uni.createFrom().publisher(FlowAdapters.toFlowPublisher(collection.find(Filters.eq("_id", id)).first()))
                .map(d -> gson.fromJson(d.toJson(), Log.class));
    }

    public Multi<Log> findAll() {
        return Multi.createFrom().publisher(FlowAdapters.toFlowPublisher(collection.find()))
                .map(d -> gson.fromJson(d.toJson(), Log.class));
    }

    public Uni<Void> deleteById(String id) {
        return Uni.createFrom().publisher(FlowAdapters.toFlowPublisher(collection.deleteOne(Filters.eq("_id", id))))
                .replaceWithVoid();
    }
}
