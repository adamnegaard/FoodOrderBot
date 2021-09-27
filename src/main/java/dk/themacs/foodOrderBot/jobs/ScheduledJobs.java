package dk.themacs.foodOrderBot.jobs;

import com.slack.api.methods.MethodsClient;
import dk.themacs.foodOrderBot.ClientHandler;
import dk.themacs.foodOrderBot.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;

import static java.util.Locale.CANADA;
import static java.util.Locale.ENGLISH;

@Component
public class ScheduledJobs {
    private final ClientHandler clientHandler;
    private final MethodsClient client;

    private final static Logger log = LoggerFactory.getLogger(ScheduledJobs.class);

    public ScheduledJobs(ClientHandler clientHandler, MethodsClient client) {
        this.clientHandler = clientHandler;
        this.client = client;
    }

    @Scheduled(cron = "0 " + AppConfig.reminderMinute + " " + AppConfig.reminderHour + " * * 1-5")
    public void sendOrderReminder() {
        log.info("Sending out an order reminder...");
        LocalDate now = LocalDate.now();
        clientHandler.sendFoodOrderReminder(client, getReminderText(now));
    }

    @Scheduled(cron = "0 " + AppConfig.orderMinute + " " + AppConfig.orderHour + " * * 1-5")
    public void sendOrderAndConfirmation() {
        log.info("Sending out the order...");
        clientHandler.orderFood(client, false);
    }

    @Scheduled(cron = "0 " + AppConfig.closingMinute + " " + AppConfig.closingHour + " * * 1-5")
    public void closeOrder() {
        log.info("closing the order...");
        clientHandler.closeOrder();
    }

    private String getReminderText(LocalDate now) {
        String daily = "Hvad kunne du tænke dig til frokost på kontoret fra baksandwich.dk i dag?\n\n" +
                "Bestillingen bliver sendt kl. " + AppConfig.orderHour + ":" + AppConfig.orderMinute + ".";
        // Monday = 1, Tuesday = 2, Wednesday = 3, Thursday = 4, Friday = 5
        DayOfWeek day = now.getDayOfWeek();
        switch (day.getValue()) {
            case 5:
                return "*#Lunch_Eins_Zwei_Freitag*\n" + daily + "\n\n *_HUSK selv at skrive om udbringning i den kommende uge!_*";
            default:
                return "*#Lunch_" + day.getDisplayName(TextStyle.FULL, ENGLISH) + "*\n" + daily;
        }
    }

}
