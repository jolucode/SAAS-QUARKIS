package service.cloud.request.clientRequest.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Client {

    private String razonSocial;
    private String wsUsuario;
    private String wsClave;
    private String wsLocation;
    private String certificadoName;
    private String certificadoPassword;
    private String usuarioSol;
    private String claveSol;
    private String integracionWs;
    private String pdfBorrador;
    private String impresion;
    private String urlOnpremise;
    private String clientId;
    private String clientSecret;
    private String UserNameSunatGuias;
    private String passwordSunatGuias;
    private EmailClientConfig email;

    @Getter
    @Setter
    public static class EmailClientConfig {
        private String replyTo;
        private String templateName;
        private boolean enabled;
    }
}