package service.cloud.request.clientRequest.estela.dto;

import lombok.Data;

@Data
public class FileRequestDTO {
    private String service;
    private String integracionWs;

    private String username;
    private String password;
    private String fileName;
    private String contentFile;

    private String ticket;

    private String rucComprobante;
    private String tipoComprobante;
    private String serieComprobante;
    private String numeroComprobante;

}
