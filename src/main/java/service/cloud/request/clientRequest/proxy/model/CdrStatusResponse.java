package service.cloud.request.clientRequest.proxy.model;

import lombok.Data;

@Data
public class CdrStatusResponse {

    protected byte[] content;

    protected String statusCode;

    protected String statusMessage;

}
