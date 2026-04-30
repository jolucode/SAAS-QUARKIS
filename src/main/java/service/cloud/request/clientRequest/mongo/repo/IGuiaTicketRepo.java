package service.cloud.request.clientRequest.mongo.repo;

import org.springframework.data.mongodb.repository.Query;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.mongo.model.GuiaTicket;

public interface IGuiaTicketRepo {

    // Consulta personalizada para devolver solo el ticketSunat basado en rucEmisor y feId
    @Query(value = "{ 'rucEmisor': ?0, 'feId': ?1 }")
    Mono<GuiaTicket> findGuiaTicketByRucEmisorAndFeId(String rucEmisor, String feId);
    Mono<Void> delete(GuiaTicket guiaTicket);
    Mono<GuiaTicket> save(GuiaTicket guiaTicket);

}
