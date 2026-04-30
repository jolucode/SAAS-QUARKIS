package service.cloud.request.clientRequest.handler.refactorPdf.dto;

import lombok.Data;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.item.InvoiceItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.legend.LegendObject;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class InvoiceObject {
    private String documentIdentifier;
    private String issueDate;
    private String dueDate;
    private String formSap;
    private String currencyValue;
    private String sunatTransaction;
    private String paymentCondition;
    private String sellOrder;
    private String sellerName;
    private String remissionGuides;
    private String datos_extra_fac;
    private String porcentajeIGV;
    private String nuevoCalculo;
    private InputStream codeQR;
    private String senderSocialReason;
    private String senderRuc;
    private String senderFiscalAddress;
    private String senderDepProvDist;
    private String senderContact;
    private String senderMail;
    private String senderLogo;
    private String Telefono;
    private String Telefono_1;
    private String Web;
    private String Comentarios;
    private String Anticipos;
    private String digestValue;
    private String receiverSocialReason;
    private String receiverRuc;
    private String receiverFiscalAddress;
    private List<InvoiceItemObject> invoiceItems;
    private List<WrapperItemObject> itemListDynamic;
    private List<WrapperItemObject> itemListDynamicC; // Numa
    private String perceptionAmount;
    private String perceptionPercentage;
    private String retentionPercentage;
    Map<String, String> invoicePersonalizacion;
    private String prepaidAmountValue;
    private String subtotalValue;
    private String igvValue;
    private String iscValue;
    private String amountValue;
    private String discountValue;
    private String totalAmountValue;
    private String gravadaAmountValue;
    private String exoneradaAmountValue;
    private String inafectaAmountValue;
    private String gratuitaAmountValue;
    private InputStream barcodeValue;
    private String letterAmountValue;
    private List<LegendObject> legends;
    private String resolutionCodeValue;
    private String validezPDF;
    private String impuestoBolsa = "0";
    private String impuestoBolsaMoneda = "PEN";
    private String C1;
    private String C2;
    private String C3;
    private String F1;
    private String F2;
    private String F3;
    private BigDecimal M1;
    private BigDecimal M2;
    private BigDecimal M3;
    private int totalCuotas;
    private BigDecimal montoPendiente;
    private String metodoPago;
    private BigDecimal baseImponibleRetencion;
    private BigDecimal porcentajeRetencion;
    private BigDecimal montoRetencion;

    public InvoiceObject() {
    }
} //InvoiceObject
