package service.cloud.request.clientRequest.estela.dto;

import lombok.Data;

@Data
public class FileResponseDTO {

    public FileResponseDTO(String status, String message, byte[] content, String ticket) {
        this.status = status;
        this.message = message;
        this.content = content;
        this.ticket = ticket;
    }

    private String status;
    private String message;
    private byte[] content;
    private String ticket;

}
