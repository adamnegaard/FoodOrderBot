package dk.themacs.foodOrderBot;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class TimeUtil {
    public static LocalDate getDateTimeFromStringInSeconds(String ts) {
        String secondsTs = ts.substring(0, ts.indexOf("."));
        return Instant.ofEpochSecond(Long.valueOf(secondsTs))
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
