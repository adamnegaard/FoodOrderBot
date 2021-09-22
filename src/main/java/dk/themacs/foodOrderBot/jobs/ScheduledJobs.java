package dk.themacs.foodOrderBot.jobs;

import com.slack.api.methods.MethodsClient;
import dk.themacs.foodOrderBot.ClientHandler;
import dk.themacs.foodOrderBot.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
        clientHandler.sendFoodOrderReminder(client, "Hvad kunne du tænke dig til frokost på kontoret fra baksandwich.dk i dag?\n" +
                "Bestilling bliver sendt kl. " + AppConfig.orderHour + ":" + AppConfig.orderMinute + ".");
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

}
