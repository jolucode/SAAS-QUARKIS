package service.cloud.request.clientRequest.mongo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(collection = "document_publications")
public class DocumentPublication {

    @Id
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