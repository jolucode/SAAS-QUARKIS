package service.cloud.request.clientRequest.service.extractor;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

public interface CloudInterface {

    Uni<Response> proccessDocument(String ejemploString);

}
