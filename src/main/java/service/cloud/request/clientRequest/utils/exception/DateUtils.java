package service.cloud.request.clientRequest.utils.exception;

import java.text.SimpleDateFormat;
import java.util.Date;
public class DateUtils {

  public static String formatDateToString(Date date) {
    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return outputFormat.format(date);
  }

}