package service.cloud.request.clientRequest.mongo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TransaccionBaja {

    @EqualsAndHashCode.Include
    private String id;
    private String rucEmpresa;
    private String fecha;
    private Integer idd;
    private String serie;
    private String ticketBaja;
    private String docId;
    // Nuevo campo para fecha y hora exacta
    private LocalDateTime fechaHora;

}
