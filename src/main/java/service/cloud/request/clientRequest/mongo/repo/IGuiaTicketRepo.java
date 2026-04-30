package service.cloud.request.clientRequest.mongo.repo;

import io.smallrye.mutiny.Uni;
import service.cloud.request.clientRequest.mongo.model.GuiaTicket;

public interface IGuiaTicketRepo {

    Uni<GuiaTicket> findGuiaTicketByRucEmisorAndFeId(String rucEmisor, String feId);
    Uni<Void> delete(GuiaTicket guiaTicket);
    Uni<GuiaTicket> save(GuiaTicket guiaTicket);

}
