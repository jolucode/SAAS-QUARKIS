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
@Document(collection = "logsPath")
public class Log {

    @Id
    @EqualsAndHashCode.Include
    private String id;
    private String ruc;
    private String businessName;
    private String request; //pathJsonRequest
    private String response;
    private String thirdPartyRequestXml; //pathThirPartyRequestXml
    private String thirdPartyResponseXml; //pathThirPartyResponseXml
    private String requestDate;
    private String responseDate;
    private String thirdPartyServiceInvocationDate;
    private String thirdPartyServiceResponseDate;
    private String objectTypeAndDocEntry;
    private String seriesAndCorrelative;

}