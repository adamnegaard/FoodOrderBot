package dk.themacs.foodOrderBot.mail.formatters;

import dk.themacs.foodOrderBot.mail.MailService;
import dk.themacs.foodOrderBot.services.PersonOrder.PersonOrderReadDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Component
public class SenderWithNumbers extends FoodOrderSender {

    public SenderWithNumbers(MailService mailService,
                             @Value("${network.credentials.username}") String mailFrom,
                             @Value("${network.credentials.cc:#{null}}") String mailCc,
                             @Value("${network.credentials.receiver}") String mailReceiver,
                             @Value("${company.name}") String companyName) {
        super(mailService, mailFrom, mailCc, mailReceiver, companyName);
    }

    @Override
    protected String getOrders(Set<PersonOrderReadDTO> personOrders) {
        Map<String, Integer> orderMap = orderSetToMap(personOrders);

        StringBuilder stringBuilder = new StringBuilder();
        for(String orderText : orderMap.keySet()) {

            // capitalise first letter in the order of the email
            String orderMessage = capitalize(orderText);

            stringBuilder.append(orderMap.get(orderText) + "X " + orderMessage + ".\n");
        }
        return stringBuilder.toString();
    }

    @Override
    protected String getHello() {
        return "Hej Betina";
    }

    @Override
    protected String getOrderModificationRequest() {
        return "Vi håber vi kan nå at rette bestillingen, så den består af følgende";
    }

    @Override
    protected String getOrderRequest() {
        return "Vi vil meget gerne bestille";
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
