package service.cloud.request.clientRequest.extras;

/**
 * Esta interfaz contiene las constantes para el proceso
 * de firmado de los documentos UBL.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public interface ISignerConfig {

    String SIGNER_PREFIX = "signer";

    String UBL_TAG_UBLEXTENSION = "ext:UBLExtension";
    String UBL_TAG_EXTENSIONCONTENT = "ext:ExtensionContent";

    String XML_TRANSFORM_KEYS_ENCODING = "UTF-8";
    String XML_TRANSFORM_KEYS_METHOD = "xml";
    String XML_TRANSFORM_KEYS_INDENT = "no";

    String SIGN_CONTEXT_NAMESPACE_PREFIX = "ds";
    String SIGNATURE_FACTORY_MECHANISM = "DOM";

}
