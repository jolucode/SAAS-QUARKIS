package service.cloud.request.clientRequest.mongo.repo;

import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.mongo.model.TransaccionBaja;

public interface ITransaccionBajaRepository {


    Mono<TransaccionBaja> findFirstByRucEmpresaOrderByFechaDescIddDesc(String rucEmpresa);

    Mono<TransaccionBaja> findFirstByRucEmpresaAndSerie(String rucEmpresa, String serie);

    // Nuevo método para buscar por rucEmpresa y docId
    Mono<TransaccionBaja> findFirstByRucEmpresaAndDocId(String rucEmpresa, String docId);
    Mono<TransaccionBaja> save(TransaccionBaja transaccionBaja);

}
