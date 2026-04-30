package service.cloud.request.clientRequest.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;

import java.util.Map;


@lombok.Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Data {


    private String ruc;
    private String docObject;
    private String docEntry;
    private String docType;
    private ResponseRequest responseRequest;
    private Error responseError;

    @lombok.Data
    public static class ResponseRequest {
        private String serviceResponse;
        Map<String, ResponseDocument> listMapDocuments;
    }

    @lombok.Data
    public static class Error {
        private ErrorRequest errorRequest;
    }

    @lombok.Data
    public static class ErrorRequest {
        private String code;
        private String description;
    }


    @AllArgsConstructor
    @lombok.Data
    public static class ResponseDocument {
        private String extension;
        private byte[] content;
    }

}



