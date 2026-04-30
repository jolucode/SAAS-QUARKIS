package service.cloud.request.clientRequest.utils.files;

import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.utils.exception.ConfigurationException;
import service.cloud.request.clientRequest.utils.exception.SignerDocumentException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Enumeration;


public class CertificateUtils {

    private final static Logger logger = Logger.getLogger(CertificateUtils.class);

    // Método para cargar un certificado en bytes desde una ruta
    public static byte[] loadCertificate(String certificatePath) throws FileNotFoundException, ConfigurationException {
        return getCertificateInBytes(certificatePath);
    }

    // Método para validar un certificado digital
    public static void validateCertificate(byte[] certificate, String password, String provider, String keystoreType) throws SignerDocumentException {
        checkDigitalCertificateV2(certificate, password, provider, keystoreType);
    }

    public static synchronized byte[] getCertificateInBytes(String certificatePath) throws ConfigurationException, FileNotFoundException {
        byte[] certificateBytes = null;
        ByteArrayOutputStream baos = null;
        FileInputStream fis = null;

        try {
            baos = new ByteArrayOutputStream();
            fis = new FileInputStream(certificatePath);

            byte[] buffer = new byte[1024];
            int read;

            while ((read = fis.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, read);
            }

            certificateBytes = baos.toByteArray();
        } catch (FileNotFoundException e) {
            logger.error("getCertificateInBytes() ERROR: " + e.getMessage());
//			throw e;
        } catch (Exception e) {
            logger.error("getCertificateInBytes() Exception(" + e.getClass().getName() + ") ERROR: " + e.getMessage());
            throw new ConfigurationException(IVenturaError.ERROR_251.getMessage());
        }
        return certificateBytes;
    }

    public static boolean checkDigitalCertificateV2(byte[] certificate, String certificatePwd, String keystoreProvider, String keystoreType) throws SignerDocumentException {
        boolean flag = false;

        try {
            if (null != certificate) {
                KeyStore keystore = KeyStore.getInstance(keystoreType, keystoreProvider);
                keystore.load(new ByteArrayInputStream(certificate), certificatePwd.toCharArray());

                String alias = getAliasFromCertificate(keystore);

                X509Certificate x509Cert = (X509Certificate) keystore.getCertificate(alias);
                if (null != x509Cert) {
                    flag = true;
                }
            } else {
                logger.error("checkDigitalCertificate() ERROR_ID: " + IVenturaError.ERROR_259.getId() + " ERROR_MSG: " + IVenturaError.ERROR_259.getMessage());
                throw new SignerDocumentException(IVenturaError.ERROR_259.getMessage());
            }
        } catch (SignerDocumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("checkDigitalCertificate() ERROR_ID: " + IVenturaError.ERROR_258.getId() + " ERROR_MSG: " + IVenturaError.ERROR_258.getMessage() + e.getMessage());
            throw new SignerDocumentException(IVenturaError.ERROR_258);
        }
        return flag;
    } //checkDigitalCertificate

    /**
     * Este metodo retorna el ALIAS del certificado digital, extrayendolo del KEYSTORE.
     * <p>
     * NOTA:
     * Se replico este metodo que viene de la clase SignerHandler, para poder
     * utilizarlo por separado y no intervenir en el proceso de hilos de la
     * Homologacion.
     * Si yo usara este metodo para el proceso de HOMOLOGACION, tendria que hacerlo
     * sincrono (synchronized) lo cual haria que intervenga en el proceso de hilos
     * de la HOMOLOGACION.
     *
     * @param keyStore El objeto KeyStore.
     * @return Retorna el ALIAS del certificado digital.
     * @throws SignerDocumentException
     */
    private static String getAliasFromCertificate(KeyStore keyStore) throws SignerDocumentException {
        String alias = null;
        try {
            Enumeration<String> aliasEnumeration = keyStore.aliases();
            while (aliasEnumeration.hasMoreElements()) {
                alias = aliasEnumeration.nextElement();
                if (keyStore.isKeyEntry(alias)) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("getAliasFromCertificate() ERROR: " + IVenturaError.ERROR_252.getMessage());
            throw new SignerDocumentException(IVenturaError.ERROR_252);
        }
        return alias;
    } //getAliasFromCertificate

} //CertificateUtils
