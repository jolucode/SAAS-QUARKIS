package service.cloud.request.clientRequest.estela.proxy;

import io.smallrye.mutiny.Uni;

public interface ServiceClient {
    Uni<String> sendSoapRequest(String url, String soapRequest);
}
