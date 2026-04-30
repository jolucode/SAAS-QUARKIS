package service.cloud.request.clientRequest.handler.refactorPdf.dto.legend;

import lombok.Data;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.WrapperItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.item.BoletaItemObject;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Data
public class BoletaObject {
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
    private String porcentajeIGV;
    private String Anticipos;
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
    private String porcentajeISC;
    private InputStream codeQR;
    private String digestValue;
    private String receiverFullname;
    private String receiverIdentifier;
    private String receiverIdentifierType;
    private String receiverFiscalAddress;
    private String PrepaidAmountValue;

    /* Lista de items */
    private List<BoletaItemObject> boletaItems;
    private List<WrapperItemObject> itemsListDynamic;

    /* Monto de Percepcion */
    private String perceptionAmount;
    private String perceptionPercentage;

    /* Montos */
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
    private String impuestoBolsa;
    private String impuestoBolsaMoneda;
    private InputStream barcodeValue;
    private String letterAmountValue;
    private List<LegendObject> legends;
    private String resolutionCodeValue;
    private Map<String, String> invoicePersonalizacion;
    private String validezPDF;

    public BoletaObject() {
    }

    public List<WrapperItemObject> getItemsDynamic() {
        return itemsListDynamic;
    }

    public void setItemsDynamic(List<WrapperItemObject> itemsDynamic) {
        this.itemsListDynamic = itemsDynamic;
    }

} //BoletaObject
