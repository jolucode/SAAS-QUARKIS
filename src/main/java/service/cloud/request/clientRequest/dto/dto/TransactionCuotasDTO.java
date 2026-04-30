package service.cloud.request.clientRequest.dto.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionCuotasDTO {
    private Integer NroOrden;
    private String TaxDate;
    private String FormaPago;
    private BigDecimal MontoCuota;
    private String DueDate;
    private String Cuota;
    private String FechaCuota;
    private String FechaEmision;
}