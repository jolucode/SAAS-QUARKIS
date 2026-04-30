package service.cloud.request.clientRequest.utils.exception;

import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;


public class ValidationException extends Exception implements java.io.Serializable {

    private static final long serialVersionUID = -3227524743909619028L;

    private ErrorObj error;

    public ValidationException(ErrorObj error) {
        super(error.getMessage());
        this.error = error;
    }


    public ErrorObj getError() {
        return error;
    } //getError

    public void setError(ErrorObj error) {
        this.error = error;
    } //setError

}
