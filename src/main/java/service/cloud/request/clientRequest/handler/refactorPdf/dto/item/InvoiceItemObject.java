package service.cloud.request.clientRequest.handler.refactorPdf.dto.item;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InvoiceItemObject {

    private BigDecimal quantityItem;
    private String unitMeasureItem;
    private String descriptionItem;
    private BigDecimal unitValueItem;
    private BigDecimal unitPriceItem;
    private String discountItem;
    private String amountItem;

    public InvoiceItemObject() {
    }
} //InvoiceItemObject
