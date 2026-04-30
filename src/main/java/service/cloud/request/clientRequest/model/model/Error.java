package service.cloud.request.clientRequest.model.model;

public class Error {

    private String cod;
    private String msg;
    private String numError;
    private String desError;

    public Error() {
    }

    public Error(String cod, String msg, String numError, String desError) {
        this.cod = cod;
        this.msg = msg;
        this.numError = numError;
        this.desError = desError;
    }

    public String getNumError() {
        return numError;
    }

    public void setNumError(String numError) {
        this.numError = numError;
    }

    public String getDesError() {
        return desError;
    }

    public void setDesError(String desError) {
        this.desError = desError;
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
}
