package service.cloud.request.clientRequest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import service.cloud.request.clientRequest.model.Client;

import java.util.HashMap;
import java.util.Optional;


@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ventura.clientes")
public class ClientProperties {

    private HashMap<String, Client> listaClientes;

    /**
     * <p>of.</p>
     *
     * @param key the error key
     * @return a Single source with NstkDefaultHeaders values
     */
    public Client listaClientesOf(String key) {
        return Optional.ofNullable(listaClientes.get(key))
                .orElseThrow(() -> new NullPointerException("El cliente con la clave '" + key + "' no existe en la lista de clientes."));
    }

}