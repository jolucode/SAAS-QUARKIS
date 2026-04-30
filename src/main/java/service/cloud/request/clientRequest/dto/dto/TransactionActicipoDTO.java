package service.cloud.request.clientRequest.dto.dto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionActicipoDTO {
    private Integer NroAnticipo;
    private String DOC_Moneda;
    private BigDecimal Anticipo_Monto;
    private String AntiDOC_Tipo;
    private String AntiDOC_Serie_Correlativo;
    private String DOC_Numero;
    private String DOC_Tipo;
}
