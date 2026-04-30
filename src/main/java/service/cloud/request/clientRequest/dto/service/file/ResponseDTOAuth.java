package service.cloud.request.clientRequest.dto.service.file;

public class ResponseDTOAuth {

    private String error_description;
    private String error;


    public ResponseDTOAuth(String error_description, String error) {
        this.error_description = error_description;
        this.error = error;
    }

    public ResponseDTOAuth() {
    }

    public String getError_description() {
        return error_description;
    }

    public void setError_description(String error_description) {
        this.error_description = error_description;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
