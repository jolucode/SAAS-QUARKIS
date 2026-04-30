/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.handler.refactorPdf.dto;

import lombok.Data;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DespatchAdviceObject {
    private String DireccionDestino;
    private String DireccionPartida;
    private String numeroGuia;
    private String obervaciones;
    private String fechaEmision;
    private String NumeroDocEmisor;
    private String numeroDocConsumidor;
    private String nombreEmisor;
    private String nombreConsumidor;
    private String fechaTraslado;
    private String codigoMotivoTraslado;
    private String descripcionMotivoTraslado;
    private String modalidadTraslado;
    private BigDecimal numeroBultos;
    private BigDecimal pesoBruto;
    private String UMPesoBruto;
    private String RUCTransportista;
    private String NombreTransportista;
    private String TipoDocumentoTransportista;
    private String placaVehiculo;
    private String TipoDocumentoConductor;
    private String DocumentoConductor;
    private String numeroContenedor;
    private String codigoEmbarque;
    private List<WrapperItemObject> itemListDynamic;
    private String resolutionCodeValue;
    private String telefono;
    private String telefono1;
    private String email;
    private String paginaWeb;
    private String validezPDF;
    private String senderLogo;
    private String licenciaConducir;
    private InputStream codeQR;

    /** 29-02-2024 Harol PDF Guia Transportista*/
    private String grtTipoDocRemitente;

    private String grtDocumentoRemitente;

    private String grtNombreRazonRemitente;

    private String grtIndicadorPagadorFlete;

    private String grtTipoDocDestinatario;

    private String grtDocumentoDestinatario;

    private String grtNombreRazonDestinatario;

    private String nroRegistroMtc;

    private String snDocIdentidadNro;

    private String snRazonSocial;

    private String nombreConductor;

    private String indicadorTransbordoProgramado;

    private String indicadorRetornoVehiculoVacio;

    private String indicadorTransporteSubcontratado;

    private String indicadorRetornoVehiculoEnvasesVacio;

    private String grtNumeroTUCEPrincipal;

    private String grtEntidadEmisoraPrincipal;

    private String grtPlacaVehiculoSecundario;

    private String grtNumeroTUCESecuendario;

    private String grtEntidadEmisoraSecundario;

    Map<String, String> despatchAdvicePersonalizacion;
}
