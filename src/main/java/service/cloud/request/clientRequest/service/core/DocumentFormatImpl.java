package service.cloud.request.clientRequest.service.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.dto.TransactionTotalesDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.service.impl.*;
import service.cloud.request.clientRequest.utils.TextUtils;
import service.cloud.request.clientRequest.utils.exception.ConfigurationException;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DocumentFormatImpl implements DocumentFormatInterface {

    Logger logger = LoggerFactory.getLogger(DocumentFormatImpl.class);

    @Inject
    InvoicePDFBuilder invoiceBuilder;

    @Inject
    BoletaPDFBuilder boletaBuilder;

    @Inject
    CreditNotePDFBuilder ncBuilder;

    @Inject
    DebitNotePDFBuilder ndBuilder;

    @Inject
    PerceptionPDFBuilder percepcionBuilder;

    @Inject
    RetentionPDFBuilder retencionBuilder;

    @Inject
    DespatchAdvicePDFBuilder despatchAdviceBuilder;

    @Override
    public byte[] createPDFDocument(UBLDocumentWRP wrp, TransacctionDTO transaction, ConfigData configuracion) throws PDFReportException {

        byte[] pdfBytes = null;
        TransacctionDTO processedTransaction = TextUtils.processAllTextFields(transaction);
        wrp.setTransaccion(processedTransaction);

        List<TransactionTotalesDTO> transaccionTotales = new ArrayList<>(transaction.getTransactionTotalesDTOList());

        String dirLogo = configuracion.getRutaBaseConfig() + transaction.getDocIdentidad_Nro() + "\\COMPANY_LOGO.jpg";
        configuracion.setCompletePathLogo(dirLogo);

        String personalizacion = transaction.getTransactionContractDocRefListDTOS().stream()
                .map(contractMap -> contractMap.get("pdf_per"))
                .filter(valor -> valor != null && !valor.isEmpty())
                .findFirst()
                .orElse("");

        try {
            switch (transaction.getDOC_Codigo()) {
                case IUBLConfig.DOC_INVOICE_CODE:
                    pdfBytes = invoiceBuilder.generateInvoicePDF(wrp, configuracion, personalizacion); //pdfHandler.generateInvoicePDF(wrp, configuracion);
                    break;

                case IUBLConfig.DOC_BOLETA_CODE:
                    pdfBytes = boletaBuilder.generateBoletaPDF(wrp, configuracion, personalizacion);
                    break;

                case IUBLConfig.DOC_CREDIT_NOTE_CODE:
                    pdfBytes = ncBuilder.generateCreditNotePDF(wrp, transaccionTotales, configuracion);
                    break;

                case IUBLConfig.DOC_DEBIT_NOTE_CODE:
                    pdfBytes = ndBuilder.generateDebitNotePDF(wrp, transaccionTotales, configuracion);
                    break;

                case IUBLConfig.DOC_PERCEPTION_CODE:
                    pdfBytes = percepcionBuilder.generatePerceptionPDF(wrp, configuracion);
                    break;

                case IUBLConfig.DOC_RETENTION_CODE:
                    pdfBytes = retencionBuilder.generateRetentionPDF(wrp, configuracion);
                    break;

                case IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE:
                case IUBLConfig.DOC_SENDER_CARRIER_GUIDE_CODE:
                    pdfBytes = despatchAdviceBuilder.generateDespatchAdvicePDF(wrp, configuracion, personalizacion);
                    break;
                default:
                    throw new ConfigurationException(IVenturaError.ERROR_460.getMessage());
            }
        } catch (ConfigurationException e) {
            logger.error(e.getMessage());
        } catch (PDFReportException e) {
            logger.error( transaction.getDocIdentidad_Nro() + "-" + transaction.getDOC_Codigo()+"-" + transaction.getDOC_Id() + " ERROR PDF: "+ e.getMessage());
            throw new PDFReportException(e.getMessage());
        }
        return pdfBytes;
    }
}
