package service.cloud.request.clientRequest.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.service.extractor.CloudService;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CloudController {

    @Inject
    private CloudService cloudService;

    @POST
    @Path("/procesar")
    public Mono<Response> processDocument(String stringRequestOnpremise) {
        return cloudService.proccessDocument(stringRequestOnpremise);
    }
}
