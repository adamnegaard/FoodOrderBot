package dk.themacs.foodOrderBot.mail;

import dk.themacs.foodOrderBot.ClientHandler;
import dk.themacs.foodOrderBot.services.PersonOrder.PersonOrderReadDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class FoodOrderSender {

    private final MailService mailService;
    private final String mailFrom;
    private final String receiverMail;
    private final String mailCc;
    private final String mailReceiver;
    private final String companyName;

    private final static Logger log = LoggerFactory.getLogger(FoodOrderSender.class);

    public FoodOrderSender(MailService mailService,
                           @Value("${network.credentials.username}") String mailFrom,
                           @Value("${network.credentials.receiver}") String receiverMail,
                           @Value("${network.credentials.cc}") String mailCc,
                           @Value("${network.credentials.receiver}") String mailReceiver,
                           @Value("${company.name}") String companyName) {
        this.mailService = mailService;
        this.mailFrom = mailFrom;
        this.receiverMail = receiverMail;
        this.mailCc = mailCc;
        this.mailReceiver = mailReceiver;
        this.companyName = companyName;
    }

    public void orderFood(Set<PersonOrderReadDTO> personOrders) throws Exception {
        String orderString = getOrders(personOrders);
        String mailContent = "Hej.\n I dag vil vi gerne bestille følgende varer:\n" +
                orderString +
                "På Forhånd tak.\n\n" +
                "Mvh" +
                "Adam Negaard";
        Mail mail = new Mail("Madbestilling: " + companyName, mailFrom, companyName, mailReceiver, mailContent, mailCc);

        try {
            mailService.sendEmail(mail);
        } catch (Exception e) {
            log.error("Cound not order food. Error occured when sending the mail. Had the following orders:\n" + orderString, e);
            throw e;
        }
    }

    private String getOrders(Set<PersonOrderReadDTO> personOrders) {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, Integer> orderMap = orderSetToMap(personOrders);

        for(String orderText : orderMap.keySet()) {
            stringBuilder.append(orderMap.get(orderText) + "X " + orderText + ".\n");
        }
        return stringBuilder.toString();
    }

    private Map<String, Integer> orderSetToMap(Set<PersonOrderReadDTO> personOrders) {
        Map<String, Integer> orders = new HashMap<>();

        for(PersonOrderReadDTO personOrder : personOrders) {
            String orderText = personOrder.getOrderText();
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
