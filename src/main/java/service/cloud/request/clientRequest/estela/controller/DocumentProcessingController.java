package service.cloud.request.clientRequest.estela.controller;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.estela.dto.FileRequestDTO;
import service.cloud.request.clientRequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientRequest.estela.service.DocumentBajaQueryService;
import service.cloud.request.clientRequest.estela.service.DocumentBajaService;
import service.cloud.request.clientRequest.estela.service.DocumentQueryService;
import service.cloud.request.clientRequest.estela.service.DocumentEmissionService;

@Path("/api/documents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentProcessingController {

    private final DocumentEmissionService documentEmissionService;
    private final DocumentQueryService documentQueryService;
    private final DocumentBajaService documentBajaService;
    private final DocumentBajaQueryService documentBajaQueryService;

    public DocumentProcessingController(DocumentEmissionService documentEmissionService, DocumentQueryService documentQueryService, DocumentBajaService documentBajaService, DocumentBajaQueryService documentBajaQueryService) {
        this.documentEmissionService = documentEmissionService;
        this.documentQueryService = documentQueryService;
        this.documentBajaService = documentBajaService;
        this.documentBajaQueryService = documentBajaQueryService;
    }

    @POST
    @Path("/upload")
    public Mono<Response> processDocumentEmission(FileRequestDTO requestDTO) {
        String url;

        // Determinar la URL según el valor del campo 'service' en requestDTO
        switch (requestDTO.getService()) {
            case "SUNAT":
                if (requestDTO.getFileName().contains("-20-") || requestDTO.getFileName().contains("-40-"))
                    url = "https://www.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService";
                else
                    url = "https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService";
                break;
            case "OSE":
                url = "https://proy.ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl";
                break;
            case "ESTELA":
                url = "https://ose-test.com/ol-ti-itcpe/billService3";
                break;
            case "BIZLINKS":
                url = "https://testose2.bizlinks.com.pe/ol-ti-itcpe/billService?wsdl";
                break;
            default:
                // En caso de que el servicio no sea uno de los conocidos, puedes asignar una URL por defecto o lanzar un error.
                return Mono.just(Response.status(Response.Status.BAD_REQUEST).entity(new FileResponseDTO("Error", "Invalid service", null, null)).build());
        }

        // Llamar al servicio con la URL determinada
        return documentEmissionService.processDocumentEmission(url, requestDTO)
                .map(response -> Response.ok(response).build())
                .defaultIfEmpty(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
    }


    @POST
    @Path("/consulta")
    public Mono<Response> processDocumentQuery(FileRequestDTO requestDTO) {
        return documentQueryService.processAndSaveFile("h-ttps://proy.ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl", requestDTO)
                .map(response -> Response.ok(response).build()).defaultIfEmpty(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
    }

    @POST
    @Path("/baja")
    public Mono<Response> processDocumentBaja(FileRequestDTO requestDTO) {
        return documentBajaService.processBajaRequest("h-ttps://proy.ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl", requestDTO)
                .map(response -> Response.ok(response).build()).defaultIfEmpty(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
    }

    @POST
    @Path("/consultabaja")
    public Mono<Response> processDocumentBajaConsult(FileRequestDTO requestDTO) {
        return documentBajaQueryService.processAndSaveFile("h-ttps://proy.ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl", requestDTO)
                .map(response -> Response.ok(response).build()).defaultIfEmpty(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
    }
}
