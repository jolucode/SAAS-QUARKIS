package service.cloud.request.clientRequest.handler.refactorPdf.dto.item;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RetentionItemObject {
    private String fechaEmision;
    private String fechaPago;
    private String tipoDocumento;
    private String nroDoc;
    private String montoTotal;
    private String monedaMontoTotal;
    private String monedaRetencion;
    private BigDecimal baseRetencion;
    private BigDecimal porcentaje;
    private String valorRetencion;
    private String montoSoles;
    private String tipoCambio;
    private String numeroPago;
    private BigDecimal importeNeto;
}
