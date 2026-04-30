package service.cloud.request.clientRequest.handler.refactorPdf.dto;

import lombok.Data;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.item.RetentionItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.legend.LegendObject;

import java.io.InputStream;
import java.util.List;

@Data
public class RetentionObject {
    private String documentIdentifier;
    private String issueDate;
    private String currencyValue;
    private String sunatTransaction;
    private String total_doc_value;
    private String senderSocialReason;
    private String senderRuc;
    private String senderFiscalAddress;
    private String senderDepProvDist;
    private String senderContact;
    private String senderMail;
    private String senderLogo;
    private String tel;
    private String tel1;
    private String web;
    private String comentarios;
    private String MontoenSoles;
    private String montoTotalDoc;
    private String validezPDF;
    private String regimenRET;
    private String receiverSocialReason;
    private String receiverRuc;
    private List<RetentionItemObject> retentionItems;
    private List<WrapperItemObject> itemListDynamic;
    private String totalAmountValue;
    private InputStream barcodeValue;
    private String digestValue;
    private InputStream codeQR;
    private String letterAmountValue;
    private List<LegendObject> legends;
    private String resolutionCodeValue;
    public RetentionObject() {
    }
}
