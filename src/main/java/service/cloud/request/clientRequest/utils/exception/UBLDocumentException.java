package service.cloud.request.clientRequest.utils.exception;

import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;

/**
 * Esta clase representa la excepcion de tipo UBL.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class UBLDocumentException extends Exception implements
        java.io.Serializable {

    private static final long serialVersionUID = 4659926513594893893L;

    private ErrorObj error;

    public UBLDocumentException(String message) {
        super(message);
    } // UBLDocumentException

    public UBLDocumentException(ErrorObj error) {
        super(error.getMessage());
        this.error = error;
    }


    public UBLDocumentException(ErrorObj error, Throwable e) {
        super(error.getMessage(), e);
        this.error = error;
    }


    public ErrorObj getError() {
        return error;
    } // getError


    public void setError(ErrorObj error) {
        this.error = error;
    } // setError

}
