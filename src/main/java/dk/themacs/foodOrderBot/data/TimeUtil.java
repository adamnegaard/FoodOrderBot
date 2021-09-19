package dk.themacs.foodOrderBot.data;

import java.time.*;

public class TimeUtil {
    public static LocalDateTime getDateTimeFromStringInSeconds(String ts) {
        String secondsTs = ts.substring(0, ts.indexOf("."));
        return Instant.ofEpochSecond(Long.valueOf(secondsTs))
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static long getSecondsFromLocalDateTime(LocalDateTime ts) {
        return ts.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
