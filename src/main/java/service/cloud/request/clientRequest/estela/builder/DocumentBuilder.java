package service.cloud.request.clientRequest.estela.builder;

import jakarta.enterprise.context.ApplicationScoped;
import service.cloud.request.clientRequest.estela.dto.FileRequestDTO;

@ApplicationScoped
public class DocumentBuilder {

    // Método para generar la estructura SOAP de emisión con valores dinámicos
    public String buildEmissionSoapRequest(FileRequestDTO requestDTO) {
        return String.format("""
                        <S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/" xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
                          <S:Header>
                            <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
                              <wsse:UsernameToken xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
                                <wsse:Username>%s</wsse:Username>
                                <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">%s</wsse:Password>
                              </wsse:UsernameToken>
                            </wsse:Security>
                          </S:Header>
                          <S:Body>
                            <ns2:sendBill xmlns:ns2="http://service.sunat.gob.pe">
                              <fileName>%s</fileName>
                              <contentFile>%s</contentFile>
                            </ns2:sendBill>
                          </S:Body>
                        </S:Envelope>
                        """,
                requestDTO.getUsername(),
                requestDTO.getPassword(),
                requestDTO.getFileName(),
                requestDTO.getContentFile());
    }

    // Método para generar la estructura SOAP de consulta con valores dinámicos
    public String buildConsultaSoapRequest(FileRequestDTO requestDTO) {
        return String.format("""
                        <S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/" xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
                          <S:Header>
                            <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
                              <wsse:UsernameToken xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
                                <wsse:Username>%s</wsse:Username>
                                <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">%s</wsse:Password>
                              </wsse:UsernameToken>
                            </wsse:Security>
                          </S:Header>
                          <S:Body>
                            <ns2:getStatusCdr xmlns:ns2="http://service.sunat.gob.pe">
                              <rucComprobante>%s</rucComprobante>
                              <tipoComprobante>%s</tipoComprobante>
                              <serieComprobante>%s</serieComprobante>
                              <numeroComprobante>%s</numeroComprobante>
                            </ns2:getStatusCdr>
                          </S:Body>
                        </S:Envelope>
                        """,
                requestDTO.getUsername(),
                requestDTO.getPassword(),
                requestDTO.getRucComprobante(),
                requestDTO.getTipoComprobante(),
                requestDTO.getSerieComprobante(),
                requestDTO.getNumeroComprobante());
    }

    // Método para generar la estructura SOAP de emisión de baja con valores dinámicos
    public String buildEmisionBajaSoapRequest(FileRequestDTO requestDTO) {
        return String.format("""
                        <S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/" xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
                          <S:Header>
                            <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
                              <wsse:UsernameToken xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
                                <wsse:Username>%s</wsse:Username>
                                <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">%s</wsse:Password>
                              </wsse:UsernameToken>
                            </wsse:Security>
                          </S:Header>
                          <S:Body>
                            <ns2:sendSummary xmlns:ns2="http://service.sunat.gob.pe">
                              <fileName>%s</fileName>
                              <contentFile>%s</contentFile>
                            </ns2:sendSummary>
                          </S:Body>
                        </S:Envelope>
                        """,
                requestDTO.getUsername(),
                requestDTO.getPassword(),
                requestDTO.getFileName(),
                requestDTO.getContentFile());
    }


    // Método para generar la estructura SOAP de consulta de bajas con valores dinámicos
    public String buildConsultaBajasSoapRequest(FileRequestDTO requestDTO) {
        return String.format("""
                        <S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/" xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
                          <S:Header>
                            <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
                              <wsse:UsernameToken xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
                                <wsse:Username>%s</wsse:Username>
                                <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">%s</wsse:Password>
                              </wsse:UsernameToken>
                            </wsse:Security>
                          </S:Header>
                          <S:Body>
                            <ns2:getStatus xmlns:ns2="http://service.sunat.gob.pe">
                              <ticket>%s</ticket>
                            </ns2:getStatus>
                          </S:Body>
                        </S:Envelope>
                        """,
                requestDTO.getUsername(),
                requestDTO.getPassword(),
                requestDTO.getTicket());
    }
    // Método para BIZLINKS: consulta con wrapper <statusCdr> requerido por su OSE
    public String buildConsultaSoapRequestBizlinks(FileRequestDTO requestDTO) {
        return String.format("""
                        <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns2="http://service.sunat.gob.pe">
                          <soap:Header>
                            <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
                              <wsse:UsernameToken xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
                                <wsse:Username>%s</wsse:Username>
                                <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">%s</wsse:Password>
                              </wsse:UsernameToken>
                            </wsse:Security>
                          </soap:Header>
                          <soap:Body>
                            <ns2:getStatusCdr xmlns:ns2="http://service.sunat.gob.pe">
                              <statusCdr>
                                <rucComprobante>%s</rucComprobante>
                                <serieComprobante>%s</serieComprobante>
                                <numeroComprobante>%s</numeroComprobante>
                                <tipoComprobante>%s</tipoComprobante>
                              </statusCdr>
                            </ns2:getStatusCdr>
                          </soap:Body>
                        </soap:Envelope>
                        """,
                requestDTO.getUsername(),
                requestDTO.getPassword(),
                requestDTO.getRucComprobante(),
                requestDTO.getSerieComprobante(),
                requestDTO.getNumeroComprobante(),
                requestDTO.getTipoComprobante());
    }

/**comentario*/

}
