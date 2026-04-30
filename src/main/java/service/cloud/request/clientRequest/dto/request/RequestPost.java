package service.cloud.request.clientRequest.dto.request;


import com.fasterxml.jackson.annotation.JsonIgnore;
import service.cloud.request.clientRequest.dto.response.Data;
import service.cloud.request.clientRequest.mongo.model.Log;

@lombok.Data
public class RequestPost {
    private int status;
    private String ruc;
    private String rucClient;
    private String docObject;
    private String docEntry;
    private String docType;
    private String ticketBaja;
    private String digestValue;
    private String barcodeValue;
    private String documentName;
    private String pathBase;
    private Data.ResponseRequest responseRequest;
    private Data.Error responseError;
    @JsonIgnore
    private String urlOnpremise;
    @JsonIgnore
    private Log logMdb;

}
