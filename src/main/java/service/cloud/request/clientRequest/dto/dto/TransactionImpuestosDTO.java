package service.cloud.request.clientRequest.dto.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionImpuestosDTO {
    private String Nombre;
    private String Abreviatura;
    private String Moneda;
    private String TipoTributo;
    private Integer LineId;
    private String TierRange;
    private BigDecimal Monto;
    private String Codigo;
    private BigDecimal ValorVenta;
    private BigDecimal Porcentaje;
    private String TipoAfectacion;
}