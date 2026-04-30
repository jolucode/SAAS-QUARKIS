package service.cloud.request.clientRequest.mongo.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import service.cloud.request.clientRequest.utils.exception.DateUtils;

import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Document(collection = "guiaTicketsSunat") // Nombre de la colección en MongoDB
public class GuiaTicket {

    @Id
    private ObjectId id; // Correspondiente a "_id" de MongoDB

    private String rucEmisor;        // RUC del emisor
    private String feId;             // Nuevo campo único que combina serie y número
    private String ticketSunat;      // Ticket devuelto por SUNAT
    private LocalDateTime fechaEmision; // Fecha de emisión del documento
    private Integer cantidadIntentos;   // Número de intentos
    private String estadoTicket;        // Estado del ticket ("PENDIENTE", "ACEPTADO", "ERROR")
    private String errorDescripcion;    // Descripción del error, si existe
    private String creadoEn;     // Fecha de creación del registro
    private String actualizadoEn; // Fecha de la última actualización
}
