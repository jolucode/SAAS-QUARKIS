package service.cloud.request.clientRequest.estela.controller;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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

    public DocumentProcessingController(DocumentEmissionService documentEmissionService,
                                        DocumentQueryService documentQueryService,
                                        DocumentBajaService documentBajaService,
                                        DocumentBajaQueryService documentBajaQueryService) {
        this.documentEmissionService = documentEmissionService;
        this.documentQueryService = documentQueryService;
        this.documentBajaService = documentBajaService;
        this.documentBajaQueryService = documentBajaQueryService;
    }

    @POST
    @Path("/upload")
    public Uni<Response> processDocumentEmission(FileRequestDTO requestDTO) {
        String url;
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
                return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                        .entity(new FileResponseDTO("Error", "Invalid service", null, null)).build());
        }
        return documentEmissionService.processDocumentEmission(url, requestDTO)
                .map(response -> Response.ok(response).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
    }

    @POST
    @Path("/consulta")
    public Uni<Response> processDocumentQuery(FileRequestDTO requestDTO) {
        return documentQueryService.processAndSaveFile("https://proy.ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl", requestDTO)
                .map(response -> Response.ok(response).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
    }

    @POST
    @Path("/baja")
    public Uni<Response> processDocumentBaja(FileRequestDTO requestDTO) {
        return documentBajaService.processBajaRequest("https://proy.ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl", requestDTO)
                .map(response -> Response.ok(response).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
    }

    @POST
    @Path("/consultabaja")
    public Uni<Response> processDocumentBajaConsult(FileRequestDTO requestDTO) {
        return documentBajaQueryService.processAndSaveFile("https://proy.ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl", requestDTO)
                .map(response -> Response.ok(response).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
    }
}
