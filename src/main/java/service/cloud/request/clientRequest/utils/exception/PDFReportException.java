package service.cloud.request.clientRequest.utils.exception;

import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;

/**
 * Esta clase representa el tipo de excepcion del emisor electronico para el
 * envio de documentos UBL a Sunat.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class PDFReportException extends Exception implements
        java.io.Serializable {

    private static final long serialVersionUID = -2062202786220686656L;

    private ErrorObj error;

    public PDFReportException(String message) {
        super(message);
    } // PDFReportException

    public PDFReportException(Throwable e) {
        super(e);
    } // PDFReportException

    public PDFReportException(String message, Throwable e) {
        super(message, e);
    } // PDFReportException

    public PDFReportException(ErrorObj error) {
        super(error.getMessage());
        this.error = error;
    }

    public PDFReportException(ErrorObj error, Throwable e) {
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
