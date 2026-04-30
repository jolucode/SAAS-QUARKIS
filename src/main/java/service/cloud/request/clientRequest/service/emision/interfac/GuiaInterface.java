package service.cloud.request.clientRequest.service.emision.interfac;

import io.smallrye.mutiny.Uni;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;

/**
 * -----------------------------------------------------------------------------

 * Proyecto          : facturación SAAS

 *                     conforme a las especificaciones de SUNAT.
 *
 * Autor             : Jose Luis Becerra
 * Rol               : Software Developer Senior
 * Fecha de creación : 09/07/2025
 * -----------------------------------------------------------------------------
 */

public interface GuiaInterface {


    public Uni<TransaccionRespuesta> transactionRemissionGuideDocumentRestReactive(TransacctionDTO transaction, String docType) throws Exception;

}
