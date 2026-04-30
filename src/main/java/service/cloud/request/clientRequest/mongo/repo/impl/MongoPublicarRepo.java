package service.cloud.request.clientRequest.mongo.repo.impl;

import com.google.gson.Gson;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.mongo.model.DocumentPublication;
import service.cloud.request.clientRequest.mongo.repo.IPublicarRepo;

@ApplicationScoped
public class MongoPublicarRepo implements IPublicarRepo {
    private final MongoCollection<Document> collection;
    private final Gson gson = new Gson();

    public MongoPublicarRepo(MongoClient mongoClient) {
        this.collection = mongoClient.getDatabase("ventura2024").getCollection("document_publications");
    }

    public Mono<DocumentPublication> save(DocumentPublication documentPublication) {
        return Mono.from(collection.insertOne(Document.parse(gson.toJson(documentPublication)))).thenReturn(documentPublication);
    }
}
