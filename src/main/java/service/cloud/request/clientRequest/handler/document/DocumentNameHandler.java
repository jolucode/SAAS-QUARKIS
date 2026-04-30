package service.cloud.request.clientRequest.handler.document;


import service.cloud.request.clientRequest.extras.IUBLConfig;

import java.text.MessageFormat;

/**
 * Esta clase contiene metodo para obtener el nombre de un documento segun el
 * formato de SUNAT.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class DocumentNameHandler {

    private static DocumentNameHandler instance = null;

    private final String DOCUMENT_PATTERN = "{0}-{1}-{2}";


    private final String EE_ZIP = ".zip";

    /**
     * Constructor privado para evitar instancias.
     */
    private DocumentNameHandler() {
    }

    /**
     * Este metodo obtiene una instancia de la clase DocumentNameHandler.
     *
     * @return Retorna una instancia de la clase DocumentNameHandler.
     */
    public static synchronized DocumentNameHandler getInstance() {
        if (null == instance) {
            instance = new DocumentNameHandler();
        }
        return instance;
    } // newInstance

    /**
     * Este metodo obtiene el nombre de un documento de tipo RETENCION.
     *
     * @param senderRUC     Numero de RUC del emisor.
     * @param docIdentifier Identificador del documento (Serie y correlativo).
     * @return Retorna el nombre del documento de tipo RETENCION.
     */
    public String getRemissionGuideName(String senderRUC, String docIdentifier) {
        return MessageFormat.format(DOCUMENT_PATTERN, senderRUC,
                IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE, docIdentifier);
    }

    /**
     * Este metodo obtiene el nombre de un documento de tipo RETENCION.
     *
     * @param senderRUC     Numero de RUC del emisor.
     * @param docIdentifier Identificador del documento (Serie y correlativo).
     * @return Retorna el nombre del documento de tipo RETENCION.
     */
    public String getRemissionGuideTransportName(String senderRUC, String docIdentifier) {
        return MessageFormat.format(DOCUMENT_PATTERN, senderRUC,
                IUBLConfig.DOC_SENDER_CARRIER_GUIDE_CODE, docIdentifier);
    }

    /**
     * Este metodo obtiene el nombre de un documento de tipo COMUNICACION DE
     * BAJA.
     *
     * @param senderRUC     Numero de RUC del emisor.
     * @param docIdentifier Identificador del documento.
     * @return Retorna el nombre del documento de tipo COMUNICACION DE BAJA.
     */
    public String getVoidedDocumentName(String senderRUC, String docIdentifier) {
        return senderRUC + "-" + docIdentifier;
    } // getVoidedDocumentName

    /**
     * Este metodo retorna el nombre del documento concatenando el valor .zip
     *
     * @param documentName El nombre del documento.
     * @return Retorna el nombre del documento concatenando el valor .zip
     */
    public String getZipName(String documentName) {
        return documentName + EE_ZIP;
    } // formatZipName



} // DocumentNameHandler
