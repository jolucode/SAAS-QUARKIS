package service.cloud.request.clientRequest.handler.refactorPdf.service;

import service.cloud.request.clientRequest.dto.dto.TransactionTotalesDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;

import java.util.List;

public interface CreditNotePDFGenerator {
    byte[] generateCreditNotePDF(UBLDocumentWRP creditNoteType, List<TransactionTotalesDTO> transaccionTotales, ConfigData configData) throws PDFReportException;
}