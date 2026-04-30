package service.cloud.request.clientRequest.mongo.repo.impl;

import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import org.reactivestreams.FlowAdapters;
import service.cloud.request.clientRequest.mongo.model.GuiaTicket;
import service.cloud.request.clientRequest.mongo.repo.IGuiaTicketRepo;

@ApplicationScoped
public class MongoGuiaTicketRepo implements IGuiaTicketRepo {
    private final MongoCollection<Document> collection;
    private final Gson gson = new Gson();

    public MongoGuiaTicketRepo(MongoClient mongoClient) {
        this.collection = mongoClient.getDatabase("ventura2024").getCollection("guiaTicketsSunat");
    }

    public Uni<GuiaTicket> findGuiaTicketByRucEmisorAndFeId(String rucEmisor, String feId) {
        return Uni.createFrom().publisher(FlowAdapters.toFlowPublisher(
                        collection.find(Filters.and(Filters.eq("rucEmisor", rucEmisor), Filters.eq("feId", feId)))
                                .sort(Sorts.descending("_id")).first()))
                .map(d -> gson.fromJson(d.toJson(), GuiaTicket.class));
    }

    public Uni<Void> delete(GuiaTicket guiaTicket) {
        return Uni.createFrom().publisher(FlowAdapters.toFlowPublisher(collection.deleteOne(Filters.eq("_id", guiaTicket.getId()))))
                .replaceWithVoid();
    }

    public Uni<GuiaTicket> save(GuiaTicket guiaTicket) {
        return Uni.createFrom().publisher(FlowAdapters.toFlowPublisher(collection.insertOne(Document.parse(gson.toJson(guiaTicket)))))
                .replaceWith(guiaTicket);
    }
}
