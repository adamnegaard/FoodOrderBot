package dk.themacs.foodOrderBot.data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeUtil {
    public static LocalDateTime getDateTimeFromStringInSeconds(String ts) {
        String secondsTs = ts.substring(0, ts.indexOf("."));
        return Instant.ofEpochSecond(Long.valueOf(secondsTs))
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
