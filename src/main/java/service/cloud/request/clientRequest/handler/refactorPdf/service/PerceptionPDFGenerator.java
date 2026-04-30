package service.cloud.request.clientRequest.handler.refactorPdf.service;

import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;

public interface PerceptionPDFGenerator {
    byte[] generatePerceptionPDF(UBLDocumentWRP perceptionType, ConfigData configData) throws PDFReportException;
}