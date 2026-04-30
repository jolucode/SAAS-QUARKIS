package service.cloud.request.clientRequest.mongo.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentPublicationDTO {
    private String id;
    private String ruc;
    private String businessName;
    private String pathPdf;
    private String pathXml;
    private String pathZip;
    private String objectTypeAndDocEntry;
    private String seriesAndCorrelative;
}
