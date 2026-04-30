package service.cloud.request.clientRequest.handler.refactorPdf.config;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.context.annotation.Configuration;

import java.io.*;

@Configuration
public class JasperReportConfig {

    // Método genérico para obtener un reporte según RUC y tipo de documento
    public JasperReport getJasperReportForRuc(String ruc, String documentName, String pathConfigBase) throws Exception {
        String reportPath = pathConfigBase + ruc + "\\formatos\\" + documentName /*+ "Document.jrxml"*/; // Ruta dinámica con RUC y tipo de documento

        File reportTemplate = new File(reportPath);
        if (!reportTemplate.isFile()) {
            throw new FileNotFoundException("Template not found for RUC " + ruc + ": " + reportPath);
        }

        InputStream inputStream = new BufferedInputStream(new FileInputStream(reportTemplate));
        JasperDesign jasperDesign = JRXmlLoader.load(inputStream);

        return JasperCompileManager.compileReport(jasperDesign);
    }
}
