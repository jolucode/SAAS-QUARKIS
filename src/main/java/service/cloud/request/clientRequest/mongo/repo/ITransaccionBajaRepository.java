package service.cloud.request.clientRequest.mongo.repo;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.mongo.model.TransaccionBaja;

public interface ITransaccionBajaRepository extends ReactiveMongoRepository<TransaccionBaja, String> {


    Mono<TransaccionBaja> findFirstByRucEmpresaOrderByFechaDescIddDesc(String rucEmpresa);

    Mono<TransaccionBaja> findFirstByRucEmpresaAndSerie(String rucEmpresa, String serie);

    // Nuevo método para buscar por rucEmpresa y docId
    Mono<TransaccionBaja> findFirstByRucEmpresaAndDocId(String rucEmpresa, String docId);

}
