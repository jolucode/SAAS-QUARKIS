package service.cloud.request.clientRequest.dto.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionLineasImpuestoDTO {
    private int LineId;
    private String NroOrden;
    private String Nombre;
    private BigDecimal ValorVenta = BigDecimal.ZERO;
    private String Abreviatura;
    private String Codigo;
    private String Moneda;
    private BigDecimal Monto = BigDecimal.ZERO;
    private BigDecimal Porcentaje;
    private String TipoTributo;
    private String TipoAfectacion;
    private String TierRange;
    private BigDecimal cantidad;
    private String unidadSunat;
}
