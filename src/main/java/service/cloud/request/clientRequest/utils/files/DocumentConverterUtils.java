package service.cloud.request.clientRequest.utils.files;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DocumentConverterUtils {

    private final static org.apache.log4j.Logger logger = Logger.getLogger(DocumentConverterUtils.class);


    // Método genérico para convertir cualquier tipo de documento a bytes
    public static <T> byte[] convertDocumentToBytes(T document) {
        return convertToBytes(document, (Class<T>) document.getClass());
    }

    // Método privado para realizar la conversión a bytes
    private static <T> byte[] convertToBytes(T document, Class<T> documentClass) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(documentClass);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // activa indentación

            // Marshal a un DOM en memoria
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            marshaller.marshal(document, doc);

            // Aplica pretty-print con indentación (Transformer)
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(doc), new StreamResult(baos));

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al convertir " + documentClass.getSimpleName() + " a byte[]", e);
        }
    }

    public static byte[] compressUBLDocument(byte[] document, String documentName) throws IOException {

        byte[] zipDocument;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(document);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(bos)) {

            byte[] buffer = new byte[10000];
            int read;

            zos.putNextEntry(new ZipEntry(documentName));

            while ((read = bis.read(buffer)) != -1) {
                zos.write(buffer, 0, read);
            }

            zos.closeEntry();
            zos.finish(); // Forzar el cierre de la entrada ZIP antes de obtener los bytes
            zipDocument = bos.toByteArray(); // Devuelve directamente los bytes comprimidos

        } catch (Exception e) {
            logger.error("Error al comprimir el documento: " + e.getMessage(), e);
            throw new IOException("Error al comprimir el documento UBL.", e);
        }

        return zipDocument;
    }
}
