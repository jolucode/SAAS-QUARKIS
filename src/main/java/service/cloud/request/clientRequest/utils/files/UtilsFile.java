package service.cloud.request.clientRequest.utils.files;


import org.eclipse.persistence.internal.oxm.ByteArrayDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.exception.VenturaExcepcion;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;

import javax.activation.DataHandler;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class UtilsFile {

    static Logger logger = LoggerFactory.getLogger(UtilsFile.class);

    public static String getAttachmentPath(TransacctionDTO transaction, String doctype, String rutaBaseDoc) {
        Calendar fecha = Calendar.getInstance();
        fecha.setTime(transaction.getDOC_FechaEmision());
        int anio = fecha.get(Calendar.YEAR);
        int mes = fecha.get(Calendar.MONTH) + 1;
        int dia = fecha.get(Calendar.DAY_OF_MONTH);

        return rutaBaseDoc
                + transaction.getDocIdentidad_Nro()
                + File.separator
                + "anexo"
                + File.separator
                + anio
                + File.separator
                + mes
                + File.separator
                + dia
                + File.separator
                + transaction.getSN_DocIdentidad_Nro()
                + File.separator
                + doctype;
    }

    /**
     * Escribe un arreglo de bytes en una ruta especificada.
     *
     * @param bytes          el contenido en bytes del archivo.
     * @param attachmentPath la ruta donde se guardará el archivo.
     * @throws IOException si ocurre algún error al escribir el archivo.
     */
    public static void saveBytesToFile(byte[] bytes, String attachmentPath) throws IOException {
        // Crear el directorio si no existe
        Path path = Paths.get(attachmentPath).getParent();
        if (path != null && !Files.exists(path)) {
            Files.createDirectories(path);
        }

        // Escribir el archivo
        try (FileOutputStream fos = new FileOutputStream(attachmentPath)) {
            fos.write(bytes);
        }
    }

    public static String storeDocumentInDisk(byte[] document, String documentName, String fileExtension, String rutaBaseDoc) throws Exception {

        String documentPath = null;
        try {
            String separator = File.separator;
            documentPath = rutaBaseDoc + separator + documentName + "." + fileExtension;

            File file = new File(rutaBaseDoc);
            if (!file.exists()) {
                file.mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(documentPath)) {
                fos.write(document);
            }

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("storeDocumentInDisk()  ERROR: " + IVenturaError.ERROR_454.getMessage() + e.getMessage());
            throw new Exception(IVenturaError.ERROR_454.getMessage());
        }

        return documentPath;
    }


    public static boolean storePDFDocumentInDisk(byte[] pdfBytes, String baseDirectory, String documentName, String extension) {

        boolean flag = false;
        try {
            String separator = File.separator;
            File file = new File(baseDirectory);
            if (!file.exists()) {
                file.mkdirs(); // Crea el directorio si no existe
            }
            String filePath = baseDirectory + separator + documentName + extension;
            File newFile = new File(filePath);
            if (!newFile.exists()) {
                newFile.createNewFile();
            } else {
                boolean canWrite = newFile.canWrite();
                if (!canWrite) {
                    throw new VenturaExcepcion("No se puede guardar el documento PDF porque está siendo usado por otro proceso. Cierre el documento y realice nuevamente el envío.");
                }
            }
            Path path = Paths.get(filePath);
            Files.write(path, pdfBytes); // Escribe los bytes en el archivo
            flag = true;
        } catch (IOException | VenturaExcepcion e) {
            logger.error("storePDFDocumentInDisk() [" + "] Exception(" + e.getClass().getName() + ") ERROR: " + e.getMessage());
        }
        return flag;
    }


    public static DataHandler compressUBLDocument(byte[] document, String documentName) throws IOException {
        DataHandler zipDocument = null;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(document);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(bos)) {

            byte[] array = new byte[StreamUtils.BUFFER_SIZE];
            int read;
            zos.putNextEntry(new ZipEntry(documentName));

            while ((read = bis.read(array)) != -1) {
                zos.write(array, 0, read);
            }
            zos.closeEntry();

            // Retornando el objeto DATAHANDLER
            zipDocument = new DataHandler(new ByteArrayDataSource(bos.toByteArray(), "application/zip"));

        } catch (Exception e) {
            throw new IOException("Error al comprimir el documento UBL: " + e.getMessage(), e);
        }

        return zipDocument;
    }

    /**
     * Guarda un String JSON en una ruta específica.
     *
     * @param jsonString El contenido del JSON como String.
     * @param filePath   La ruta donde se desea guardar el archivo (incluye el nombre del archivo).
     * @throws IOException Si ocurre un error al guardar el archivo.
     */
    public static void saveJsonToFile(String jsonString, String filePath) throws IOException {
        // Verificar que el jsonString y filePath no sean nulos o vacíos
        if (jsonString == null || jsonString.isEmpty()) {
            throw new IllegalArgumentException("El contenido JSON no puede ser nulo o vacío.");
        }

        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo no puede ser nula o vacía.");
        }

        // Crear directorios si no existen
        Path path = Paths.get(filePath).getParent(); // Solo la carpeta
        //logger.info("Ruta archivo JSON baja: " + path);
        if (path != null && Files.notExists(path)) {
            Files.createDirectories(path);
        }

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(jsonString);
            logger.info("Archivo JSON guardado exitosamente en: " + filePath);
        }
    }

}
