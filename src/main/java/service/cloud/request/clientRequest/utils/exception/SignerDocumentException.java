package service.cloud.request.clientRequest.utils.exception;

import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;


public class SignerDocumentException extends Exception implements
        java.io.Serializable {

    private static final long serialVersionUID = 4659926513594893893L;

    private ErrorObj error;


    public SignerDocumentException(String message) {
        super(message);
    } // SignerDocumentException


    public SignerDocumentException(ErrorObj error) {
        super(error.getMessage());
        this.error = error;
    } // SignerDocumentException


    public ErrorObj getError() {
        return error;
    } // getError


    public void setError(ErrorObj error) {
        this.error = error;
    } // setError

} // SignerDocumentException
