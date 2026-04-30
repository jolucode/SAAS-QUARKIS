package service.cloud.request.clientRequest.handler.refactorPdf.dto;

import lombok.Data;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.item.PerceptionItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.legend.LegendObject;

import java.io.InputStream;
import java.util.List;

@Data
public class PerceptionObject {
    private String documentIdentifier;
    private String issueDate;
    private String currencyValue;
    private String sunatTransaction;
    private String senderSocialReason;
    private String senderRuc;
    private String senderFiscalAddress;
    private String senderDepProvDist;
    private String senderContact;
    private String senderMail;
    private String senderLogo;
    private String telValue;
    private String tel2Value;
    private String webValue;
    private String comments;
    private String importeTexto;
    private String digestValue;
    private InputStream codeQR;
    private String receiverSocialReason;
    private String receiverRuc;
    private List<PerceptionItemObject> perceptionItems;
    private List<WrapperItemObject> itemListDynamic;
    private String totalAmountValue;
    private InputStream barcodeValue;
    private String letterAmountValue;
    private List<LegendObject> legends;
    private String resolutionCodeValue;
    private String validezPDF;

    public PerceptionObject() {
    }
}
