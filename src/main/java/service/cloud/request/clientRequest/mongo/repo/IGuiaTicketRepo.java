package service.cloud.request.clientRequest.mongo.repo;

import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.mongo.model.GuiaTicket;

public interface IGuiaTicketRepo {

    // Consulta personalizada para devolver solo el ticketSunat basado en rucEmisor y feId
    Mono<GuiaTicket> findGuiaTicketByRucEmisorAndFeId(String rucEmisor, String feId);
    Mono<Void> delete(GuiaTicket guiaTicket);
    Mono<GuiaTicket> save(GuiaTicket guiaTicket);

}
