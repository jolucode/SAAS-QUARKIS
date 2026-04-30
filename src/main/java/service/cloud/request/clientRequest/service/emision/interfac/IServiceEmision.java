package service.cloud.request.clientRequest.service.emision.interfac;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
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

@Component
public interface IServiceEmision {


    /**emision*/
    public Mono<TransaccionRespuesta> transactionDocument(TransacctionDTO transaction, String doctype);



}
