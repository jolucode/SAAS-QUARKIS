package service.cloud.request.clientRequest.service.core;

import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientRequest.handler.FileHandler;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;

public interface ProcessorCoreInterface {

    TransaccionRespuesta processCDRResponseV2(byte[] cdrConstancy, byte[] signedDocument, UBLDocumentWRP documentWRP, TransacctionDTO transaction, ConfigData configuracion, String documentName, String attachmentPath) throws Exception;

    TransaccionRespuesta processResponseSinCDR( UBLDocumentWRP documentWRP, TransacctionDTO transaction,ConfigData configuracion, FileResponseDTO cdrStatusResponse);

    public byte[] processCDRResponseContigencia(byte[] cdrConstancy,
                                                FileHandler fileHandler, String documentName,
                                                String documentCode, UBLDocumentWRP documentWRP,
                                                TransacctionDTO transaccion, ConfigData configuracion) throws PDFReportException;
}
