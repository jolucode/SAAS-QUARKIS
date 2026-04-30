package service.cloud.request.clientRequest.dto.dto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionComprobantesDTO {
    private BigDecimal CP_ImporteTotal;
    private String CP_MonedaMontoNeto;
    private String TC_MonedaObj;
    private String CP_Moneda;
    private BigDecimal U_TC_Factor;
    private Integer NroOrden;
    private String U_TipoMoneda;
    private BigDecimal U_PagoImporteSR;
    private BigDecimal CP_Importe;
    private String U_DOC_Tipo;
    private String DOC_Numero;
    private String DOC_FechaEmision;
    private BigDecimal DOC_Importe;
    private String U_DOC_Moneda;
    private String PagoFecha;
    private String PagoNumero;
    private String PagoMoneda;
    private String U_CP_Fecha;
    private String TC_MonedaRef;
    private String TC_Fecha;

    // Campos agregados que faltaban en la clase original
    private String U_TC_MonedaRef;
    private String U_CP_Moneda;
    private String U_PagoMoneda;
    private String DOC_Tipo;
    private String U_Moneda;
    private Integer U_NroOrden;
    private String CP_Fecha;
    private BigDecimal U_CP_Importe;
    private String U_PagoFecha;
    private String U_TipoComprobante;
    private String U_NroPago;
    private String U_TC_Fecha;
    private BigDecimal PagoImporteSR;
    private String U_TC_MonedaObj;
    private String U_CP_MonedaMontoNeto;
    private String U_TipoMonedaTotal;
    private String U_DOC_FechaEmision;
    private BigDecimal TC_Factor;
    private BigDecimal U_CP_ImporteTotal;
    private Integer U_PagoNumero;
    private String U_DOC_Numero;
    private BigDecimal U_DOC_Importe;
    private BigDecimal U_Importe_Pago_Soles;
}
