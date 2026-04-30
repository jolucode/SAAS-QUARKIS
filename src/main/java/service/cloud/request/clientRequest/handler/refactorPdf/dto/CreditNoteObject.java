package service.cloud.request.clientRequest.handler.refactorPdf.dto;

import lombok.Data;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.legend.LegendObject;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class CreditNoteObject {
    private String documentIdentifier;
    private String issueDate;
    private String dueDate;
    private String currencyValue;
    private String sunatTransaction;
    private String paymentCondition;
    private String sellOrder;
    private String sellerName;
    private String remissionGuides;
    private String Telefono;
    private String Telefono1;
    private String Web;
    private String porcentajeIGV;
    private String Comentarios;
    private String perceptionPercentage;
    private String ISCPercetange;
    private String perceptionAmount;
    private InputStream codeQR;
    private String digestValue;
    private String typeOfCreditNote;
    private String descOfCreditNote;
    private String documentReferenceToCn;
    private String dateDocumentReference;
    private String senderSocialReason;
    private String senderRuc;
    private String senderFiscalAddress;
    private String senderDepProvDist;
    private String senderContact;
    private String senderMail;
    private String senderLogo;
    private String receiverRegistrationName;
    private String receiverIdentifier;
    private String receiverIdentifierType;
    private String receiverFiscalAddress;
    private List<WrapperItemObject> itemsListDynamic;
    private List<WrapperItemObject> itemListDynamicC; // Numa
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
    private String validezPDF;
    private InputStream barcodeValue;
    private String letterAmountValue;
    private List<LegendObject> legends;
    private Map<String, String> invoicePersonalizacion;
    private String resolutionCodeValue;
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

    public CreditNoteObject() {
    }
} //CreditNoteObject
