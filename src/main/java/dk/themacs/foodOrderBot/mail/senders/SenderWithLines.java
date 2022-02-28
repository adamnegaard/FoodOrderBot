package dk.themacs.foodOrderBot.mail.senders;

import dk.themacs.foodOrderBot.entities.PersonOrder;
import dk.themacs.foodOrderBot.mail.MailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component("SenderWithLines")
public class SenderWithLines extends FoodOrderSender  {

    public SenderWithLines(MailService mailService,
                             @Value("${network.credentials.username}") String mailFrom,
                             @Value("${network.credentials.cc:#{null}}") String mailCc,
                             @Value("${network.credentials.receiver}") String mailReceiver,
                             @Value("${company.name}") String companyName) {
        super(mailService, mailFrom, mailCc, mailReceiver, companyName);
    }

    @Override
    protected String getOrders(Set<PersonOrder> personOrders) {
        StringBuilder stringBuilder = new StringBuilder();
        for(PersonOrder personOrder : personOrders) {
            String orderText = personOrder.getOrderText();

            // capitalise first letter in the order of the email
            String orderMessage = capitalize(orderText);

            stringBuilder.append(orderMessage + ".\n");
        }
        return stringBuilder.toString();
    }

    @Override
    protected String getHello() {
        return "Hej BAK";
    }

    @Override
    protected String getOrderModificationRequest() {
        return "Ups, vi håber vi kan nå at tilføje noget ekstra, så vores bestilling består af følgende";
    }

    @Override
    protected String getOrderRequest() {
        return "Idag kunne vi godt tænke os";
    }
}
