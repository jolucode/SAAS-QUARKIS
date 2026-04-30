package service.cloud.request.clientRequest.mongo.repo.impl;

import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.mongo.model.Log;
import service.cloud.request.clientRequest.mongo.repo.ILogRepo;

@ApplicationScoped
public class MongoLogRepo implements ILogRepo {
    private final MongoCollection<Document> collection;
    private final Gson gson = new Gson();

    public MongoLogRepo(MongoClient mongoClient) {
        this.collection = mongoClient.getDatabase("ventura2024").getCollection("logsPath");
    }

    public Mono<Log> save(Log log) { return Mono.from(collection.insertOne(Document.parse(gson.toJson(log)))).thenReturn(log); }
    public Mono<Log> findById(String id) { return Mono.from(collection.find(Filters.eq("_id", id)).first()).map(d -> gson.fromJson(d.toJson(), Log.class)); }
    public Flux<Log> findAll() { return Flux.from(collection.find()).map(d -> gson.fromJson(d.toJson(), Log.class)); }
    public Mono<Void> deleteById(String id) {
        return Mono.from(collection.deleteOne(Filters.eq("_id", id))).then();
    }
}
