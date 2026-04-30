package service.cloud.request.clientRequest.handler.refactorPdf.dto.item;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PerceptionItemObject {
    private String tipoDocumento;
    private String fechaEmision;
    private String numSerieDoc;
    private BigDecimal precioVenta;
    private BigDecimal importePercepcion;
    private BigDecimal montoTotalCobrado;
    private BigDecimal porcentajePercepcion;
    private String monedaPercepcion;
    private String monedaMontoTotal;
}
