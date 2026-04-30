/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.dto;

import lombok.Data;
import service.cloud.request.clientRequest.mongo.model.LogDTO;

import java.util.List;

/**
 * @author VSUser
 */

@Data
public class TransaccionRespuesta {

    public static final int DEFAULT = -10000;

    public static final int RQT_EMITDO_ESPERA = 100;

    private int codigo = DEFAULT;


    private String mensaje = "No enviado";


    private String identificador;


    private byte[] pdf;

    private byte[] pdfBorrador;

    private byte[] xml;

    private byte[] zip;

    private String digestValue;

    private String barcodeValue;

    private Sunat sunat;

    private String estado;

    private String ticketRest;

    private LogDTO logDTO;

    private String errorPdf;

    public byte[] getPdfBorrador() {
        return pdfBorrador;
    }

    public void setPdfBorrador(byte[] pdfBorrador) {
        this.pdfBorrador = pdfBorrador;
    }

    public TransaccionRespuesta() {

    }

    public String getTicketRest() {
        return ticketRest;
    }

    public void setTicketRest(String ticketRest) {
        this.ticketRest = ticketRest;
    }

    public TransaccionRespuesta(int codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }


    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }


    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }


    /**
     * @return the pdf
     */
    public byte[] getPdf() {
        return pdf;
    }

    /**
     * @param pdf the pdf to set
     */
    public void setPdf(byte[] pdf) {
        this.pdf = pdf;
    }

    /**
     * @return the xml
     */
    public byte[] getXml() {
        return xml;
    }

    /**
     * @param xml the xml to set
     */
    public void setXml(byte[] xml) {
        this.xml = xml;
    }

    /**
     * @return the zip
     */
    public byte[] getZip() {
        return zip;
    }

    /**
     * @param zip the zip to set
     */
    public void setZip(byte[] zip) {
        this.zip = zip;
    }

    /**
     * @return the sunat
     */
    public Sunat getSunat() {
        return sunat;
    }

    /**
     * @param sunat the sunat to set
     */
    public void setSunat(Sunat sunat) {
        this.sunat = sunat;
    }

    public static class Observacion {

        private int codObservacion;

        private String msjObservacion;

        @Override
        public int hashCode() {
            int hash = 7;
            return hash;
        }

        public int getCodObservacion() {
            return codObservacion;
        }

        public void setCodObservacion(int codObservacion) {
            this.codObservacion = codObservacion;
        }

        public String getMsjObservacion() {
            return msjObservacion;
        }

        public void setMsjObservacion(String msjObservacion) {
            this.msjObservacion = msjObservacion;
        }

    }

    public static class Sunat {

        private int codigo = -1;

        private String mensaje = "";

        private String id = "";

        private boolean aceptado = false;

        private List<Observacion> listaObs;

        public String getEmisor() {
            return emisor;
        }

        public void setEmisor(String emisor) {
            this.emisor = emisor;
        }

        private String emisor;

        public boolean isAceptado() {
            return aceptado;
        }

        /**
         * @return the codigo
         */
        public int getCodigo() {
            return codigo;
        }

        /**
         * @param codigo the codigo to set
         */
        public void setCodigo(int codigo) {
            this.codigo = codigo;
            aceptado = (this.codigo == 0);
        }

        /**
         * @return the mensaje
         */
        public String getMensaje() {
            return mensaje;
        }

        /**
         * @param mensaje the mensaje to set
         */
        public void setMensaje(String mensaje) {
            this.mensaje = mensaje;
        }

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * @return the List Observacoin
         */
        public List<Observacion> getListaObs() {
            return listaObs;
        }

        public void setListaObs(List<Observacion> listaObs) {
            this.listaObs = listaObs;
        }

        @Override
        public String toString() {
            return "[" + this.id + "]-(" + this.codigo + ") " + this.mensaje;
        }

        public void setAceptado(boolean aceptado) {
            this.aceptado = aceptado;
        }
    }

}
