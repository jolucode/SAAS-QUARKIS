package service.cloud.request.clientRequest.mongo.repo.impl;

import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.mongo.model.GuiaTicket;
import service.cloud.request.clientRequest.mongo.repo.IGuiaTicketRepo;

@ApplicationScoped
public class MongoGuiaTicketRepo implements IGuiaTicketRepo {
    private final MongoCollection<Document> collection;
    private final Gson gson = new Gson();

    public MongoGuiaTicketRepo(MongoClient mongoClient) {
        this.collection = mongoClient.getDatabase("ventura2024").getCollection("guiaTicketsSunat");
    }

    public Mono<GuiaTicket> findGuiaTicketByRucEmisorAndFeId(String rucEmisor, String feId) {
        return Mono.from(collection.find(Filters.and(Filters.eq("rucEmisor", rucEmisor), Filters.eq("feId", feId)))
                        .sort(Sorts.descending("_id")).first())
                .map(d -> gson.fromJson(d.toJson(), GuiaTicket.class));
    }

    public Mono<Void> delete(GuiaTicket guiaTicket) {
        return Mono.from(collection.deleteOne(Filters.eq("_id", guiaTicket.getId()))).then();
    }

    public Mono<GuiaTicket> save(GuiaTicket guiaTicket) {
        return Mono.from(collection.insertOne(Document.parse(gson.toJson(guiaTicket)))).thenReturn(guiaTicket);
    }
}
