package service.cloud.request.clientRequest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import service.cloud.request.clientRequest.model.Client;

import java.util.Optional;

@Component
public class ProviderProperties {

    @Autowired
    ApplicationProperties applicationProperties;

    @Autowired
    ClientProperties clientProperties;

    public String getRutaBaseDoc() {
        return applicationProperties.getRutaBaseDocAnexos();
    }

    public String getUrlOnpremise(String ruc) {
        return clientProperties.listaClientesOf(ruc).getUrlOnpremise();
    }


    public Client getClientProperties(String key) {
        return Optional.ofNullable(clientProperties.listaClientesOf(key)).orElse(null);
    }








}
