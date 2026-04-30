package service.cloud.request.clientRequest.utils;

import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    // Definir múltiples formatos de fecha
    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd", // Formato original
            "dd/MM/yyyy"  // Nuevo formato
    };

    public static Date parseDate(String dateString) {
        for (String format : DATE_FORMATS) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            try {
                return dateFormat.parse(dateString);
            } catch (ParseException e) {
                // Continuar intentando con los siguientes formatos
            }
        }
        // Si ninguno de los formatos funciona, manejar el error
        System.err.println("Formato de fecha no reconocido: " + dateString);
        return null;
    }

    public static String formatIssueDate(XMLGregorianCalendar xmlGregorianCal) throws Exception {

        Date inputDate = xmlGregorianCal.toGregorianCalendar().getTime();
        Locale locale = new Locale(IPDFCreatorConfig.LOCALE_ES, IPDFCreatorConfig.LOCALE_PE);
        SimpleDateFormat sdf = new SimpleDateFormat(IPDFCreatorConfig.PATTERN_DATE, locale);
        String issueDate = sdf.format(inputDate);

        return issueDate;
    }

}
