package service.cloud.request.clientRequest.mongo.repo;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.mongo.model.GuiaTicket;

public interface IGuiaTicketRepo extends ReactiveMongoRepository<GuiaTicket, String> {

    // Consulta personalizada para devolver solo el ticketSunat basado en rucEmisor y feId
    @Query(value = "{ 'rucEmisor': ?0, 'feId': ?1 }")
    Mono<GuiaTicket> findGuiaTicketByRucEmisorAndFeId(String rucEmisor, String feId);

}
