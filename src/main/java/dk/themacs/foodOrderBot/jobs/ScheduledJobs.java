package dk.themacs.foodOrderBot.jobs;

import ch.qos.logback.core.util.CachingDateFormatter;
import dk.themacs.foodOrderBot.ClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ScheduledJobs {
    private final ClientHandler clientHandler;

    private final static Logger log = LoggerFactory.getLogger(ScheduledJobs.class);

    public ScheduledJobs(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Scheduled(cron = "0 0 8 * * 1-5")
    //@Scheduled(cron = "0 49 21 * * 0-5")
    public void sendOrderReminder() {
        log.info("Sending out an order reminder...");
        clientHandler.sendFoodOrderReminder("Hvad kunne du tænke dig til frokost på kontoret fra baksandwich.dk i dag?\n" +
                "Bestilling bliver sendt kl. 9:30.");
    }

    @Scheduled(cron = "0 30 9 * * 1-5")
    //@Scheduled(cron = "0 50 21 * * 0-5")
    public void sendOrderAndConfirmation() {
        log.info("Sending out the order...");
        clientHandler.orderFood();
    }

}
