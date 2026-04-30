package service.cloud.request.clientRequest.service.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientRequest.extras.ISunatConnectorConfig;
import service.cloud.request.clientRequest.handler.FileHandler;
import service.cloud.request.clientRequest.utils.Constants;
import service.cloud.request.clientRequest.utils.SunatResponseUtils;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;

@Service
public class ProcessorCoreImpl implements ProcessorCoreInterface {

    Logger logger = LoggerFactory.getLogger(ProcessorCoreImpl.class);

    private final String docUUID = Constants.DOC_UUID;

    @Autowired
    DocumentFormatInterface documentFormatInterface;


    @Override
    public TransaccionRespuesta processCDRResponseV2(byte[] statusResponse, byte[] signedDocument,
                                                     UBLDocumentWRP documentWRP,
                                                     TransacctionDTO transaction, ConfigData configuracion, String documentName, String attachmentPath) throws Exception {

        TransaccionRespuesta transactionResponse = null;
        TransaccionRespuesta.Sunat sunatResponse = SunatResponseUtils.proccessResponse(statusResponse, transaction, configuracion.getIntegracionWs());//proccessResponse(cdrConstancy, transaction, configuracion.getIntegracionWs());

        if ((IVenturaError.ERROR_0.getId() == sunatResponse.getCodigo()) || (4000 <= sunatResponse.getCodigo())) {

            byte[] pdfBytes = null;
            String errorPdf = null;
            try {
                pdfBytes = documentFormatInterface.createPDFDocument(documentWRP, transaction, configuracion);
            } catch (Exception e) {
                errorPdf = e.getMessage();
            }


            transactionResponse = new TransaccionRespuesta();
            transactionResponse.setCodigo(TransaccionRespuesta.RQT_EMITDO_ESPERA);

            if (errorPdf != null)
                transactionResponse.setMensaje(sunatResponse.getMensaje() + " - " + errorPdf);
            else {
                transactionResponse.setMensaje(sunatResponse.getMensaje());
            }
            transactionResponse.setSunat(sunatResponse);
            transactionResponse.setXml(signedDocument);
            transactionResponse.setZip(statusResponse);

            if(pdfBytes!=null)
                transactionResponse.setPdf(pdfBytes);

            transactionResponse.setDigestValue(SunatResponseUtils.extractDigestValue(statusResponse));
        } else {
            //documento rechazado
            transactionResponse = new TransaccionRespuesta();
            transactionResponse.setMensaje(sunatResponse.getMensaje());
            transactionResponse.setSunat(sunatResponse);
            transactionResponse.setXml(signedDocument);
            transactionResponse.setZip(statusResponse);
        }


        saveAllFiles(transactionResponse, documentName, attachmentPath);
        return transactionResponse;
    } //processCDRResponse

    private void saveAllFiles(TransaccionRespuesta transactionResponse, String documentName, String attachmentPath) throws Exception {
        FileHandler fileHandler = FileHandler.newInstance(this.docUUID);
        fileHandler.setBaseDirectory(attachmentPath);
        if(transactionResponse.getXml()!=null)
            fileHandler.storeDocumentInDisk(transactionResponse.getXml(), documentName, "xml");
        if(transactionResponse.getPdf()!=null)
            fileHandler.storeDocumentInDisk(transactionResponse.getPdf(), documentName, "pdf");
        if(transactionResponse.getZip()!=null)
            fileHandler.storeDocumentInDisk(transactionResponse.getZip(), documentName, "zip");
    }

    @Override
    public TransaccionRespuesta processResponseSinCDR(UBLDocumentWRP documentWRP, TransacctionDTO transaction, ConfigData configuracion,FileResponseDTO responseDTO) {
        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
        transactionResponse.setMensaje(responseDTO.getMessage());
        byte[] pdfBytes = null;
        try {
            pdfBytes = documentFormatInterface.createPDFDocument( documentWRP, transaction, configuracion);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        transactionResponse.setPdf(pdfBytes);
        return transactionResponse;
    }

    @Override
    public byte[] processCDRResponseContigencia(byte[] cdrConstancy, FileHandler fileHandler,
                                                String documentName, String documentCode, UBLDocumentWRP documentWRP, TransacctionDTO
                                                        transaccion, ConfigData configuracion) throws PDFReportException {
        byte[] pdfBytes = null;
        try {
            pdfBytes = documentFormatInterface.createPDFDocument( documentWRP, transaccion, configuracion);
            if (null != pdfBytes && 0 < pdfBytes.length) {
                fileHandler.storePDFDocumentInDisk(pdfBytes, documentName, ISunatConnectorConfig.EE_PDF);
            } else {
                logger.error("processCDRResponse() [" + this.docUUID + "] " + IVenturaError.ERROR_461.getMessage());
            }
        } catch (Exception e) {
            throw new PDFReportException(e.getMessage());
        }
        return pdfBytes;
    }

}
