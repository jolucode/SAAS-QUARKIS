package service.cloud.request.clientRequest.utils;

import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.creditnote_2.CreditNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.debitnote_2.DebitNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.despatchadvice_2.DespatchAdviceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.invoice_2.InvoiceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.perception_1.PerceptionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.retention_1.RetentionType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DocumentConfig {

    public static final Map<String, Class<?>> DOCUMENT_TYPE_MAP = Map.of(
            IUBLConfig.DOC_INVOICE_CODE, InvoiceType.class,
            IUBLConfig.DOC_BOLETA_CODE, InvoiceType.class,
            IUBLConfig.DOC_CREDIT_NOTE_CODE, CreditNoteType.class,
            IUBLConfig.DOC_DEBIT_NOTE_CODE, DebitNoteType.class,
            IUBLConfig.DOC_RETENTION_CODE, RetentionType.class,
            IUBLConfig.DOC_PERCEPTION_CODE, PerceptionType.class,
            IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE, DespatchAdviceType.class
    );

    // Cache de JAXBContext
    private static final Map<Class<?>, JAXBContext> JAXB_CONTEXT_CACHE = new ConcurrentHashMap<>();

    public static JAXBContext getJAXBContext(Class<?> clazz) {
        return JAXB_CONTEXT_CACHE.computeIfAbsent(clazz, c -> {
            try {
                return JAXBContext.newInstance(c);
            } catch (JAXBException e) {
                throw new RuntimeException("Error inicializando JAXBContext para " + c, e);
            }
        });
    }
}
