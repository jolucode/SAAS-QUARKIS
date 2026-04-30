package service.cloud.request.clientRequest.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class DateConverter {

    public static String convertToDate(String dateStr) throws Exception {
        Date date = null;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            date = formatter.parse(dateStr);
        } catch (ParseException e1) {
            try {
                XMLGregorianCalendar xmlGregorianCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateStr);
                date = xmlGregorianCal.toGregorianCalendar().getTime();
            } catch (Exception e2) {
                throw new IllegalArgumentException("Unsupported date format: " + dateStr);
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }

    public static String convertToDate(XMLGregorianCalendar xmlGregorianCal) {
        Date date = xmlGregorianCal.toGregorianCalendar().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }

    public static String convertToDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }

    public static String convertToDate(Object dateObj) throws Exception {
        if (dateObj instanceof String) {
            return convertToDate((String) dateObj);
        } else if (dateObj instanceof XMLGregorianCalendar) {
            return convertToDate((XMLGregorianCalendar) dateObj);
        } else if (dateObj instanceof Date) {
            return convertToDate((Date) dateObj);
        } else {
            throw new IllegalArgumentException("Unsupported date object type: " + dateObj.getClass().getName());
        }
    }

}
