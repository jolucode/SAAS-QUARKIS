package service.cloud.request.clientRequest.model.model;

import lombok.Data;

import java.util.List;

@Data
public class ResponseDTO {

    //Http
    private int statusCode;

    //Response JWT
    private String access_token;
    private String token_type;
    private int expires_in;

    //Response JWT 401
    private String status;
    private String message;

    //Response Declarar OK
    private String numTicket;
    private String fecRecepcion;

    //Response Declare ERROR
    private String cod;
    private String msg;
    private List<Error> errors;

    //Response Error Consultar
    private String codRespuesta;
    private Error error;
    private String arcCdr;
    private String indCdrGenerado;

    //response error 400
    private ResponseDTOAuth responseDTO400;

    public ResponseDTO() {
    }

    public ResponseDTO(int statusCode, String access_token, String token_type, int expires_in, String status, String message, String numTicket, String fecRecepcion, String cod, String msg, List<Error> errors, String codRespuesta, Error error, String arcCdr, String indCdrGenerado, ResponseDTOAuth responseDTO400) {
        this.statusCode = statusCode;
        this.access_token = access_token;
        this.token_type = token_type;
        this.expires_in = expires_in;
        this.status = status;
        this.message = message;
        this.numTicket = numTicket;
        this.fecRecepcion = fecRecepcion;
        this.cod = cod;
        this.msg = msg;
        this.errors = errors;
        this.codRespuesta = codRespuesta;
        this.error = error;
        this.arcCdr = arcCdr;
        this.indCdrGenerado = indCdrGenerado;
        this.responseDTO400 = responseDTO400;
    }

    public ResponseDTOAuth getResponseDTO400() {
        return responseDTO400;
    }

    public void setResponseDTO400(ResponseDTOAuth responseDTO400) {
        this.responseDTO400 = responseDTO400;
    }

    public String getCodRespuesta() {
        return codRespuesta;
    }

    public void setCodRespuesta(String codRespuesta) {
        this.codRespuesta = codRespuesta;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public String getArcCdr() {
        return arcCdr;
    }

    public void setArcCdr(String arcCdr) {
        this.arcCdr = arcCdr;
    }

    public String getIndCdrGenerado() {
        return indCdrGenerado;
    }

    public void setIndCdrGenerado(String indCdrGenerado) {
        this.indCdrGenerado = indCdrGenerado;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public String getNumTicket() {
        return numTicket;
    }

    public void setNumTicket(String numTicket) {
        this.numTicket = numTicket;
    }

    public String getFecRecepcion() {
        return fecRecepcion;
    }

    public void setFecRecepcion(String fecRecepcion) {
        this.fecRecepcion = fecRecepcion;
    }

    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
