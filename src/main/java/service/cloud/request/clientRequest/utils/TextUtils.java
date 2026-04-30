package service.cloud.request.clientRequest.utils;

import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.dto.TransactionLineasDTO;
import java.util.Map;


public class TextUtils {

    /**
     * Procesa saltos de línea para JasperReports
     * Convierte \r y \n a saltos de línea reales
     * También maneja caracteres literales \\r y \\n que vienen como strings
     */
    public static String processLineBreaks(String text) {
        if (text == null) return null;

        // Primero convertir literales escapados (\\r \\n) a saltos reales
        // Luego normalizar todos los saltos de línea a \n
        return text.replace("\\r\\n", "\n")  // Maneja cadena literal \\r\\n
                .replace("\\n", "\n")         // Maneja cadena literal \\n
                .replace("\\r", "\n")         // Maneja cadena literal \\r
                .replace("\r\n", "\n")        // Normaliza CRLF Windows
                .replace("\r", "\n")          // Normaliza CR Mac antiguo
                .replace(String.valueOf((char)13), "\n"); // Carriage Return ASCII
    }

    /**
     * Procesa texto para HTML (si se usa markup HTML en JasperReports)
     */
    public static String processLineBreaksForHtml(String text) {
        if (text == null) return null;

        // Detectar patrón donde los saltos se convirtieron en espacios
        // Usar regex para encontrar patrones como "PALABRA NUMERO" o "LINEA SALTO"
        String result = text;

        // Patrón: espacio seguido de palabra que indica nueva línea
        result = result.replaceAll(" (SALTO DE LINEA \\d+)", "<br/>$1")
                .replaceAll("(LINEA) (SALTO)", "$1<br/>$2");

        // Procesar caracteres de control reales también
        return result.replace("\\r", "<br/>")
                .replace("\\n", "<br/>")
                .replace("\r\n", "<br/>")
                .replace("\r", "<br/>")
                .replace("\n", "<br/>")
                .replace(String.valueOf((char)13), "<br/>")
                .replace(String.valueOf((char)10), "<br/>");
    }

    /**
     * Procesa campos específicos de TransacctionDTO - Versión optimizada
     */
    public static TransacctionDTO processAllTextFields(TransacctionDTO transaction) {
        if (transaction == null) return null;

        // Campos principales
        transaction.setFE_Comentario(processLineBreaks(transaction.getFE_Comentario()));
        transaction.setRazonSocial(processLineBreaks(transaction.getRazonSocial()));
        transaction.setSN_RazonSocial(processLineBreaks(transaction.getSN_RazonSocial()));
        transaction.setDIR_NomCalle(processLineBreaks(transaction.getDIR_NomCalle()));
        transaction.setSN_DIR_NomCalle(processLineBreaks(transaction.getSN_DIR_NomCalle()));
        transaction.setSN_DIR_Direccion(processLineBreaks(transaction.getSN_DIR_Direccion()));
        transaction.setDIR_Direccion(processLineBreaks(transaction.getDIR_Direccion()));
        transaction.setPersonContacto(processLineBreaks(transaction.getPersonContacto()));
        transaction.setNombreComercial(processLineBreaks(transaction.getNombreComercial()));
        transaction.setSN_NombreComercial(processLineBreaks(transaction.getSN_NombreComercial()));

        // Procesar mapas de contratos (cu06, obs, etc.)
        if (transaction.getTransactionContractDocRefListDTOS() != null) {
            for (Map<String, String> map : transaction.getTransactionContractDocRefListDTOS()) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String value = entry.getValue();
                    if (value != null) {
                        // Usar el método processLineBreaks para mantener consistencia
                        String processed = processLineBreaks(value);
                        map.put(entry.getKey(), processed);
                    }
                }
            }
        }

        // Procesar líneas de transacción
        if (transaction.getTransactionLineasDTOList() != null) {
            for (TransactionLineasDTO linea : transaction.getTransactionLineasDTOList()) {
                linea.setDescripcion(processLineBreaks(linea.getDescripcion()));

                // Procesar campos de usuario en líneas
                if (linea.getTransaccionLineasCamposUsuario() != null) {
                    for (Map.Entry<String, String> entry : linea.getTransaccionLineasCamposUsuario().entrySet()) {
                        String value = entry.getValue();
                        if (value instanceof String) {
                            linea.getTransaccionLineasCamposUsuario().put(entry.getKey(), processLineBreaks(value));
                        }
                    }
                }
            }
        }

        return transaction;
    }

    /**
     *  Reemplazar saltos de linea en los campos del objeto Transaccion para poder generar correctamente el XMl
     *  */
    public static String processLineBreaksForXML(String text) {
        if (text == null) return null;

        return text.replace("\\r\\n", " ")  // Maneja cadena literal \\r\\n
                .replace("\\n", " ")         // Maneja cadena literal \\n
                .replace("\\r", " ")         // Maneja cadena literal \\r
                .replace("\r\n", " ")        // Normaliza CRLF Windows
                .replace("\r", " ")          // Normaliza CR Mac antiguo
                .replace(String.valueOf((char)13), " "); // Carriage Return ASCII
    }

    public static TransacctionDTO processAllTextFieldsXML(TransacctionDTO transaction) {
        if (transaction == null) return null;

        // Campos principales
        transaction.setFE_Comentario(processLineBreaksForXML(transaction.getFE_Comentario()));
        transaction.setRazonSocial(processLineBreaksForXML(transaction.getRazonSocial()));
        transaction.setSN_RazonSocial(processLineBreaksForXML(transaction.getSN_RazonSocial()));
        transaction.setDIR_NomCalle(processLineBreaksForXML(transaction.getDIR_NomCalle()));
        transaction.setSN_DIR_NomCalle(processLineBreaksForXML(transaction.getSN_DIR_NomCalle()));
        transaction.setSN_DIR_Direccion(processLineBreaksForXML(transaction.getSN_DIR_Direccion()));
        transaction.setDIR_Direccion(processLineBreaksForXML(transaction.getDIR_Direccion()));
        transaction.setPersonContacto(processLineBreaksForXML(transaction.getPersonContacto()));
        transaction.setNombreComercial(processLineBreaksForXML(transaction.getNombreComercial()));
        transaction.setSN_NombreComercial(processLineBreaksForXML(transaction.getSN_NombreComercial()));

        // Procesar mapas de contratos (cu06, obs, etc.)
        if (transaction.getTransactionContractDocRefListDTOS() != null) {
            for (Map<String, String> map : transaction.getTransactionContractDocRefListDTOS()) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String value = entry.getValue();
                    if (value != null) {
                        // Usar el método processLineBreaks para mantener consistencia
                        String processed = processLineBreaksForXML(value);
                        map.put(entry.getKey(), processed);
                    }
                }
            }
        }

        // Procesar líneas de transacción
        if (transaction.getTransactionLineasDTOList() != null) {
            for (TransactionLineasDTO linea : transaction.getTransactionLineasDTOList()) {
                linea.setDescripcion(processLineBreaksForXML(linea.getDescripcion()));

                // Procesar campos de usuario en líneas
                if (linea.getTransaccionLineasCamposUsuario() != null) {
                    for (Map.Entry<String, String> entry : linea.getTransaccionLineasCamposUsuario().entrySet()) {
                        String value = entry.getValue();
                        if (value instanceof String) {
                            linea.getTransaccionLineasCamposUsuario().put(entry.getKey(), processLineBreaksForXML(value));
                        }
                    }
                }
            }
        }

        return transaction;
    }

}
