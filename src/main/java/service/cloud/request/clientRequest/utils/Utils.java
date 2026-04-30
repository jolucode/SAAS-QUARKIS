package service.cloud.request.clientRequest.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    static Logger logger = LoggerFactory.getLogger(Utils.class);

    public static final XMLGregorianCalendar stringDateToDateGregory(Date date) {
        XMLGregorianCalendar xmlDate = null;
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(format.format(date));

        } catch (Exception e) {
            logger.error("Este es error en XMLGregorian -> " + e.getMessage());
        }
        return xmlDate;
    }

    public static BigDecimal round(BigDecimal value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        value = value.setScale(places, RoundingMode.HALF_UP);
        return value;
    }

    public static boolean isNullOrTrimmedEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }


    public static String construirSerie(String prefijo, String fecha, String nuevoId) {
        return prefijo + fecha + "-" + nuevoId;
    }

}
