package service.cloud.request.clientRequest.dto.wrapper;

import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.creditnote_2.CreditNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.debitnote_2.DebitNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.despatchadvice_2.DespatchAdviceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.invoice_2.InvoiceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.perception_1.PerceptionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.retention_1.RetentionType;

public class UBLDocumentWRP {

    private TransacctionDTO transaccion;
    private InvoiceType invoiceType;
    private InvoiceType boletaType;
    private CreditNoteType creditNoteType;
    private DebitNoteType debitNoteType;
    private PerceptionType perceptionType;
    private RetentionType retentionType;
    private DespatchAdviceType adviceType;

    // Getters y Setters
    public TransacctionDTO getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(TransacctionDTO transaccion) {
        this.transaccion = transaccion;
    }

    public InvoiceType getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(InvoiceType invoiceType) {
        this.invoiceType = invoiceType;
    }

    public InvoiceType getBoletaType() {
        return boletaType;
    }

    public void setBoletaType(InvoiceType boletaType) {
        this.boletaType = boletaType;
    }

    public CreditNoteType getCreditNoteType() {
        return creditNoteType;
    }

    public void setCreditNoteType(CreditNoteType creditNoteType) {
        this.creditNoteType = creditNoteType;
    }

    public DebitNoteType getDebitNoteType() {
        return debitNoteType;
    }

    public void setDebitNoteType(DebitNoteType debitNoteType) {
        this.debitNoteType = debitNoteType;
    }

    public PerceptionType getPerceptionType() {
        return perceptionType;
    }

    public void setPerceptionType(PerceptionType perceptionType) {
        this.perceptionType = perceptionType;
    }

    public RetentionType getRetentionType() {
        return retentionType;
    }

    public void setRetentionType(RetentionType retentionType) {
        this.retentionType = retentionType;
    }

    public DespatchAdviceType getAdviceType() {
        return adviceType;
    }

    public void setAdviceType(DespatchAdviceType adviceType) {
        this.adviceType = adviceType;
    }

}
