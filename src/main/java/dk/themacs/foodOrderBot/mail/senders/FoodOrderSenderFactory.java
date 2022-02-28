package dk.themacs.foodOrderBot.mail.senders;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;

import static java.util.Locale.ENGLISH;

@Component
public class FoodOrderSenderFactory {

    private final FoodOrderSender senderWithNumbers;
    private final FoodOrderSender senderWithLines;

    public FoodOrderSenderFactory(@Qualifier("SenderWithNumbers") FoodOrderSender senderWithNumbers,
                                  @Qualifier("SenderWithLines") FoodOrderSender senderWithLines) {
        this.senderWithNumbers = senderWithNumbers;
        this.senderWithLines = senderWithLines;
    }

    public FoodOrderSender getFoodOrderSenderForToday(){
        return getFoodOrderSender(getDayOfWeek());
    }

    private FoodOrderSender getFoodOrderSender(DayOfWeek dayOfWeek){
        return switch (dayOfWeek) {
            case TUESDAY, WEDNESDAY, FRIDAY -> senderWithNumbers;
            case MONDAY, THURSDAY -> senderWithLines;
            default -> throw new IllegalArgumentException("No food order sender for day of week: " + dayOfWeek.getDisplayName(TextStyle.FULL, ENGLISH));
        };
    }

    private DayOfWeek getDayOfWeek() {
        LocalDate now = LocalDate.now();
        return now.getDayOfWeek();
    }
}
