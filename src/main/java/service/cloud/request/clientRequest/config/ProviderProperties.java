package service.cloud.request.clientRequest.config;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import service.cloud.request.clientRequest.model.Client;

import java.util.Optional;

@ApplicationScoped
public class ProviderProperties {

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
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
