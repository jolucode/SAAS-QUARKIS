package service.cloud.request.clientRequest.handler.refactorPdf.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class WrapperItemObject {
    private List<String> lstDinamicaItem;
    private Map<String, String> lstItemHashMap;
}
