package service.cloud.request.clientRequest.mongo.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogDTO {

  private String id;
  private String ruc;
  private String businessName;
  private String request;
  private String response;
  private String pathThirdPartyRequestXml;
  private String pathThirdPartyResponseXml;
  private String requestDate;
  private String responseDate;
  private String thirdPartyServiceInvocationDate;
  private String thirdPartyServiceResponseDate;
  private String objectTypeAndDocEntry;
  private String seriesAndCorrelative;
  private String pathBase;
}
