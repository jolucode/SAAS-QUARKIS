package service.cloud.request.clientRequest.notification.adapter;

import service.cloud.request.clientRequest.notification.dto.EmailMessage;

/**
 * Contrato único para todos los proveedores de email.
 * Agrega un nuevo proveedor implementando esta interfaz + @ConditionalOnProperty.
 * Cambia de proveedor solo con notification.provider en configProcesador.yml.
 */
public interface EmailProvider {

    String send(EmailMessage message) throws Exception;

    String providerName();
}
