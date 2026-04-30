package service.cloud.request.clientRequest.mongo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DocumentPublication {

    @EqualsAndHashCode.Include
    private String id;
    private String ruc;
    private String businessName;
    private String pathPdf;
    private String pathXml;
    private String pathZip;
    private String objectTypeAndDocEntry;
    private String seriesAndCorrelative;

}
