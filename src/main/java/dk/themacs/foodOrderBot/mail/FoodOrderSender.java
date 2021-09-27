package dk.themacs.foodOrderBot.mail;

import dk.themacs.foodOrderBot.ClientHandler;
import dk.themacs.foodOrderBot.services.PersonOrder.PersonOrderReadDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FoodOrderSender {

    private final MailService mailService;
    private final String mailFrom;
    private final String mailCc;
    private final String mailReceiver;
    private final String companyName;

    private final static Logger log = LoggerFactory.getLogger(FoodOrderSender.class);

    public FoodOrderSender(MailService mailService,
                           @Value("${network.credentials.username}") String mailFrom,
                           @Value("${network.credentials.cc:#{null}}") String mailCc,
                           @Value("${network.credentials.receiver}") String mailReceiver,
                           @Value("${company.name}") String companyName) {
        this.mailService = mailService;
        this.mailFrom = mailFrom;
        this.mailCc = mailCc;
        this.mailReceiver = mailReceiver;
        this.companyName = companyName;
    }

    public String mailContent(Set<PersonOrderReadDTO> personOrders, boolean lateOrder) {
        String orderString = getOrders(personOrders);
        return "Hej Betina\n" +
                (lateOrder ? "Vi håber vi kan nå at rette bestillingen, så den består af følgende" : "Vi vil meget gerne bestille:") + ":\n\n" +
                orderString + "\n" +
                "Tak!\n\n" +
                "Mvh\n" +
                companyName;
    }

    public void orderFood(String mailContent) throws Exception {
        Mail mail = new Mail("Madbestilling: " + companyName, mailFrom, companyName, mailReceiver, mailContent, mailCc);

        try {
            mailService.sendEmail(mail);
        } catch (Exception e) {
            log.error("Cound not order food. Error occured when sending the mail. Had the following mail prepared:\n" + mailContent, e);
            throw e;
        }
    }

    private String getOrders(Set<PersonOrderReadDTO> personOrders) {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, Integer> orderMap = orderSetToMap(personOrders);

        for(String orderText : orderMap.keySet()) {
            //Capitalise first letter in the order of the email
            String orderMessage = orderText.substring(0, 1).toUpperCase(Locale.ROOT) + orderText.substring(1);
            stringBuilder.append(orderMap.get(orderText) + "X " + orderMessage + ".\n");
        }
        return stringBuilder.toString();
    }

    private Map<String, Integer> orderSetToMap(Set<PersonOrderReadDTO> personOrders) {
        Map<String, Integer> orders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for(PersonOrderReadDTO personOrder : personOrders) {
            // Convert them all to lowercase
            String orderText = personOrder.getOrderText().toLowerCase(Locale.ROOT);

            if(orders.containsKey(orderText)) {
                // Overwrite the existing order
                orders.put(orderText, orders.get(orderText) + 1);
            } else {
                // Add a new entry in the map
                orders.put(orderText, 1);
            }
        }
        return orders;
    }
}
