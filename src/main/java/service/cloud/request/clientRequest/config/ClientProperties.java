package service.cloud.request.clientRequest.config;

import lombok.Getter;
import lombok.Setter;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.yaml.snakeyaml.Yaml;
import service.cloud.request.clientRequest.model.Client;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Getter
@Setter
@ApplicationScoped
public class ClientProperties {

    private HashMap<String, Client> listaClientes = new HashMap<>();

    @PostConstruct
    void init() {
        try {
            Path cfg = Path.of("configProcesador.yml");
            if (!Files.exists(cfg)) {
                return;
            }
            try (InputStream in = Files.newInputStream(cfg)) {
                Yaml yaml = new Yaml();
                Map<String, Object> root = yaml.load(in);
                Object ventura = root.get("ventura");
                if (!(ventura instanceof Map<?, ?> venturaMap)) return;
                Object clientes = venturaMap.get("clientes");
                if (!(clientes instanceof Map<?, ?> clientesMap)) return;
                Object lista = clientesMap.get("listaClientes");
                if (!(lista instanceof Map<?, ?> listaMap)) return;

                for (Map.Entry<?, ?> e : listaMap.entrySet()) {
                    String key = String.valueOf(e.getKey());
                    if (e.getValue() instanceof Map<?, ?> valueMap) {
                        Client c = new Client();
                        c.setRazonSocial(str(valueMap, "razonSocial"));
                        c.setWsUsuario(str(valueMap, "wsUsuario"));
                        c.setWsClave(str(valueMap, "wsClave"));
                        c.setWsLocation(str(valueMap, "wsLocation"));
                        c.setUsuarioSol(str(valueMap, "usuarioSol"));
                        c.setClaveSol(str(valueMap, "claveSol"));
                        c.setCertificadoName(str(valueMap, "certificadoName"));
                        c.setCertificadoPassword(str(valueMap, "certificadoPassword"));
                        c.setIntegracionWs(str(valueMap, "integracionWs"));
                        c.setPdfBorrador(str(valueMap, "pdfBorrador"));
                        c.setImpresion(str(valueMap, "impresion"));
                        c.setUrlOnpremise(str(valueMap, "urlOnpremise"));
                        c.setClientId(str(valueMap, "clientId"));
                        c.setClientSecret(str(valueMap, "clientSecret"));
                        c.setUserNameSunatGuias(str(valueMap, "UserNameSunatGuias"));
                        c.setPasswordSunatGuias(str(valueMap, "passwordSunatGuias"));
                        Object emailObj = valueMap.get("email");
                        if (emailObj instanceof Map<?, ?> em) {
                            Client.EmailClientConfig ec = new Client.EmailClientConfig();
                            ec.setReplyTo(str(em, "replyTo"));
                            ec.setTemplateName(str(em, "templateName"));
                            Object enabled = em.get("enabled");
                            ec.setEnabled(enabled != null && Boolean.parseBoolean(String.valueOf(enabled)));
                            c.setEmail(ec);
                        }
                        listaClientes.put(key, c);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

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

    private String str(Map<?, ?> map, String key) {
        Object v = map.get(key);
        return v == null ? "" : String.valueOf(v);
    }

}
