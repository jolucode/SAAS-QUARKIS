package service.cloud.request.clientRequest.dto.dto;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class TransacctionDTO {
    private String SN_NombreComercial;
    private String SN_DIR_NomCalle;
    private String SN_DocIdentidad_Nro;
    private String SN_EMail;
    private String DIR_NomCalle;
    private String SN_DIR_Distrito;
    private String NombreComercial;
    private String DOC_FechaVencimiento;
    private BigDecimal DOC_ImporteTotal;
    private String RazonSocial;
    private String TipoOperacionSunat;
    private String REFDOC_MotivCode;
    private String SUNAT_Transact;
    private String FE_Estado;
    private BigDecimal DOC_SinPercepcion = new BigDecimal("0.0");
    private String DocIdentidad_Tipo;
    private String DIR_Distrito;
    private String SN_DIR_Direccion;
    private String DocIdentidad_Nro;
    private String FE_FormSAP;
    @SerializedName(value = "DOC_ImpuestoTotal", alternate = {"DOCImpuestoTotal"})
    private BigDecimal DOC_ImpuestoTotal = new BigDecimal("0.0");
    private String SN_EMail_Secundario;
    private String DIR_Direccion;
    private String SN_RazonSocial;
    private String DOC_Serie;
    private String DOC_CondPago;
    private String DOC_PorcImpuesto;
    private BigDecimal DOC_PorDescuento;
    private String DIR_Ubigeo;
    private String DOC_Id;
    private String FE_Id;
    private String SN_DIR_Pais;
    private String REFDOC_MotivDesc;
    private String PersonContacto;
    private String FE_ObjectType;
    private String DIR_Pais;
    private String SN_DocIdentidad_Tipo;
    private String ANTICIPO_Id;
    private String MonedaPagado;
    private String Telefono;
    private String Telefono_1;
    private BigDecimal ANTICIPO_Monto = new BigDecimal("0.0");
    private String REFDOC_Tipo;
    private String Web;
    private String SN_DIR_Ubigeo;
    private String RET_Regimen;
    private String RET_Tasa;
    private String Observacione;
    private String FechaDOCRef;
    private BigDecimal DOC_OtrosCargos;
    private BigDecimal DOC_Descuento;
    private String DOC_Codigo;
    private Date DOC_FechaEmision;
    private BigDecimal DOC_DescuentoTotal;
    private String DOC_MON_Codigo;
    private String SN_DIR_Departamento;
    private String REFDOC_Id;
    private String CuentaDetraccion;
    private String CodigoDetraccion;
    private String PorcDetraccion;
    private String MontoDetraccion;
    private String CodigoPago;
    private String Ticket_Baja;
    private String MontoRetencion;
    private String SN_DIR_Provincia;
    private String EMail;
    private String SN_SegundoNombre;
    private BigDecimal DOC_MontoTotal;
    private String DIR_Provincia;
    private String FE_Comentario;
    private String DIR_Departamento;
    private String FE_TipoTrans;
    private Integer FE_DocEntry;
    private String DOC_MON_Nombre;
    private BigDecimal DOC_Importe;
    private BigDecimal ImportePagado;
    private BigDecimal DOC_MonPercepcion;
    private BigDecimal DOC_PorPercepcion;
    private String DOC_Numero;

    private List<Map<String, String>> transactionContractDocRefListDTOS;
    private List<TransactionPropertiesDTO> transactionPropertiesDTOList;
    private List<TransactionImpuestosDTO> transactionImpuestosDTOList;
    private List<TransactionLineasDTO> transactionLineasDTOList;
    private TransactionGuiasDTO transactionGuias;
    private List<TransactionCuotasDTO> transactionCuotasDTOList;
    private List<TransactionDocReferDTO> transactionDocReferDTOList;
    private List<TransactionTotalesDTO> transactionTotalesDTOList;
    private List<TransactionComprobantesDTO> transactionComprobantesDTOList;
    private List<TransactionActicipoDTO> transactionActicipoDTOList;
}








