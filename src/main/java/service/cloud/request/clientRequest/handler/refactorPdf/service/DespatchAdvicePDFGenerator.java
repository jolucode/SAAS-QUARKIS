package service.cloud.request.clientRequest.handler.refactorPdf.service;

import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;

public interface DespatchAdvicePDFGenerator {
    byte[] generateDespatchAdvicePDF(UBLDocumentWRP despatchAdvice, ConfigData configData, String personalizacion) throws PDFReportException;
}

