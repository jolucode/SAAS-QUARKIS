package service.cloud.request.clientRequest.mongo.repo;

import io.smallrye.mutiny.Uni;
import service.cloud.request.clientRequest.mongo.model.TransaccionBaja;

public interface ITransaccionBajaRepository {

    Uni<TransaccionBaja> findFirstByRucEmpresaOrderByFechaDescIddDesc(String rucEmpresa);

    Uni<TransaccionBaja> findFirstByRucEmpresaAndSerie(String rucEmpresa, String serie);

    Uni<TransaccionBaja> findFirstByRucEmpresaAndDocId(String rucEmpresa, String docId);

    Uni<TransaccionBaja> save(TransaccionBaja transaccionBaja);

}
