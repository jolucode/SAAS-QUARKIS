package service.cloud.request.clientRequest.mongo.repo.impl;

import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.mongo.model.TransaccionBaja;
import service.cloud.request.clientRequest.mongo.repo.ITransaccionBajaRepository;

@ApplicationScoped
public class MongoTransaccionBajaRepository implements ITransaccionBajaRepository {
    private final MongoCollection<Document> collection;
    private final Gson gson = new Gson();

    public MongoTransaccionBajaRepository(MongoClient mongoClient) {
        this.collection = mongoClient.getDatabase("ventura2024").getCollection("transaccion_baja");
    }

    public Mono<TransaccionBaja> findFirstByRucEmpresaOrderByFechaDescIddDesc(String rucEmpresa) {
        return Mono.from(collection.find(Filters.eq("rucEmpresa", rucEmpresa))
                        .sort(Sorts.orderBy(Sorts.descending("fecha"), Sorts.descending("idd"))).first())
                .map(this::toModel);
    }

    public Mono<TransaccionBaja> findFirstByRucEmpresaAndSerie(String rucEmpresa, String serie) {
        return Mono.from(collection.find(Filters.and(Filters.eq("rucEmpresa", rucEmpresa), Filters.eq("serie", serie)))
                        .sort(Sorts.orderBy(Sorts.descending("fechaHora"), Sorts.descending("idd"))).first())
                .map(this::toModel);
    }

    public Mono<TransaccionBaja> findFirstByRucEmpresaAndDocId(String rucEmpresa, String docId) {
        return Mono.from(collection.find(Filters.and(Filters.eq("rucEmpresa", rucEmpresa), Filters.eq("docId", docId)))
                        .sort(Sorts.orderBy(Sorts.descending("fechaHora"), Sorts.descending("idd"))).first())
                .map(this::toModel);
    }

    public Mono<TransaccionBaja> save(TransaccionBaja transaccionBaja) {
        Document d = Document.parse(gson.toJson(transaccionBaja));
        return Mono.from(collection.insertOne(d)).thenReturn(transaccionBaja);
    }

    private TransaccionBaja toModel(Document d) {
        return gson.fromJson(d.toJson(), TransaccionBaja.class);
    }
}
