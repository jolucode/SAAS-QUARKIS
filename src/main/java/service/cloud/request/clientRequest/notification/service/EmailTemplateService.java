package service.cloud.request.clientRequest.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import service.cloud.request.clientRequest.config.ApplicationProperties;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class EmailTemplateService {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateService.class);

    private static final String DEFAULT_TEMPLATE = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family:Arial,sans-serif;color:#333;margin:0;padding:0">
              <div style="max-width:600px;margin:0 auto;padding:24px">
                <h2 style="color:#1a73e8;margin-bottom:16px">Comprobante Electrónico</h2>
                <p>Estimado/a <strong>{{recipientName}}</strong>,</p>
                <p>Le informamos que <strong>{{emisorName}}</strong> ha emitido el siguiente comprobante:</p>
                <table style="width:100%;border-collapse:collapse;margin:16px 0">
                  <tr style="background:#f5f5f5">
                    <td style="padding:10px;border:1px solid #ddd;font-weight:bold">Tipo</td>
                    <td style="padding:10px;border:1px solid #ddd">{{documentType}}</td>
                  </tr>
                  <tr>
                    <td style="padding:10px;border:1px solid #ddd;font-weight:bold">Número</td>
                    <td style="padding:10px;border:1px solid #ddd">{{documentSeries}}</td>
                  </tr>
                  <tr style="background:#f5f5f5">
                    <td style="padding:10px;border:1px solid #ddd;font-weight:bold">Monto Total</td>
                    <td style="padding:10px;border:1px solid #ddd">{{currency}} {{amount}}</td>
                  </tr>
                </table>
                <p>Consulte y descargue su documento aquí:</p>
                <p>
                  <a href="{{portalUrl}}"
                     style="background:#1a73e8;color:#fff;padding:12px 24px;text-decoration:none;border-radius:4px;display:inline-block">
                    Ver mi documento
                  </a>
                </p>
                <hr style="border:none;border-top:1px solid #eee;margin:24px 0">
                <p style="font-size:12px;color:#888">
                  Este correo fue generado automáticamente. No responda a este mensaje.<br>
                  Si tiene consultas, comuníquese con <strong>{{emisorName}}</strong>.
                </p>
              </div>
            </body>
            </html>
            """;

    private static final Map<String, String> TIPO_DOC = Map.of(
            "01", "Factura",
            "03", "Boleta de Venta",
            "07", "Nota de Crédito",
            "08", "Nota de Débito",
            "09", "Guía de Remisión",
            "20", "Comprobante de Retención",
            "40", "Comprobante de Percepción"
    );

    @Inject
    private ApplicationProperties appProperties;

    public String buildSubject(String emisorName, String docType, String docSeries) {
        String tipo = TIPO_DOC.getOrDefault(docType, "Comprobante");
        return String.format("[%s] %s — %s", tipo, docSeries, emisorName);
    }

    public String buildBody(String ruc, String templateName, Map<String, String> tokens) {
        String template = loadTemplate(ruc, templateName);
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue() != null ? entry.getValue() : "");
        }
        return template;
    }

    public Map<String, String> buildTokens(String recipientName, String emisorName,
                                           String docType, String docSeries,
                                           BigDecimal amount, String currency, String portalUrl) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("es", "PE"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return Map.of(
                "recipientName", recipientName != null ? recipientName : "Cliente",
                "emisorName", emisorName != null ? emisorName : "",
                "documentType", TIPO_DOC.getOrDefault(docType, "Comprobante"),
                "documentSeries", docSeries != null ? docSeries : "",
                "amount", amount != null ? nf.format(amount) : "0.00",
                "currency", currency != null ? currency : "PEN",
                "portalUrl", portalUrl != null ? portalUrl : ""
        );
    }

    private String loadTemplate(String ruc, String templateName) {
        if (templateName == null || templateName.isBlank()) return DEFAULT_TEMPLATE;
        try {
            Path path = Path.of(appProperties.getRutaBaseDocConfig(), ruc, templateName + ".html");
            if (Files.exists(path)) {
                return Files.readString(path);
            }
        } catch (IOException e) {
            log.warn("No se pudo leer la plantilla de email para RUC {}: {}", ruc, e.getMessage());
        }
        return DEFAULT_TEMPLATE;
    }
}
