package service.cloud.request.clientRequest.config;

import lombok.Getter;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Arrays;
import java.util.List;

@Getter
@ApplicationScoped
public class ApplicationProperties {

    @ConfigProperty(name = "certificate.supplierCertificate")
    public String supplierCertificate;

    @ConfigProperty(name = "certificate.keystoreCertificateType")
    public String keystoreCertificateType;

    @ConfigProperty(name = "application.ambiente")
    public String ambiente;

    @ConfigProperty(name = "application.rutas.rutaBaseDoc-anexos")
    public String rutaBaseDocAnexos;

    @ConfigProperty(name = "application.rutas.rutaBaseDoc-config")
    public String rutaBaseDocConfig;

    @ConfigProperty(name = "application.soap-client.sunat.base-url-emision")
    public String urlSunatEmision;

    @ConfigProperty(name = "application.soap-client.sunat.base-url-consulta")
    public String urlSunatConsulta;

    @ConfigProperty(name = "application.soap-client.sunat.base-url-retper")
    public String urlSunatRetencion;

    @ConfigProperty(name = "application.soap-client.ose.base-url")
    public String urlOse;

    @ConfigProperty(name = "application.soap-client.estela.base-url")
    public String urlEstela;

    @ConfigProperty(name = "application.soap-client.bizlinks.base-url")
    public String urlBizlinks;

    /**
     * Método para determinar la URL en base a los parámetros dados.
     *
     * @param client       Cliente: SUNAT , OSE, ESTELA, BIZLINKS
     * @param feTipoTrans  Tipo de transacción: E (emisión) o B (baja)
     * @param feEstado       Estado: N (nuevo) o C (consulta)
     * @param tipoDoc      Tipo de documento: '01', '03', '07', '08', '20', '40', etc.
     * @return La URL correspondiente según los parámetros.
     */
    public String obtenerUrl(String client, String feEstado, String feTipoTrans, String tipoDoc) {
        // Validación de parámetros
        if (client == null || feTipoTrans == null || feEstado == null || tipoDoc == null) {
            throw new IllegalArgumentException("Ninguno de los parámetros puede ser nulo.");
        }

        // Parámetros posibles
        List<String> documentosEmision = Arrays.asList("01", "03", "07", "08");
        List<String> documentosPerRet = Arrays.asList("20", "40");

        // Lógica para SUNAT
        if ("SUNAT".equalsIgnoreCase(client)) {
            if ("N".equalsIgnoreCase(feEstado) && ("E".equalsIgnoreCase(feTipoTrans) || "B".equalsIgnoreCase(feTipoTrans))) {
                if (documentosEmision.contains(tipoDoc)) {
                    return urlSunatEmision;
                } else if (documentosPerRet.contains(tipoDoc)) {
                    return urlSunatRetencion;
                }
            } else if ("C".equalsIgnoreCase(feEstado)) {
                return urlSunatConsulta;
            }
        }

        if ("OSE".equalsIgnoreCase(client)) {
                return urlOse;
        }

        if ("ESTELA".equalsIgnoreCase(client)) {
                return urlEstela;
        }

        if ("BIZLINKS".equalsIgnoreCase(client)) {
                return urlBizlinks;
        }

        throw new IllegalArgumentException("No se pudo determinar la URL con los parámetros proporcionados.");
    }
}

