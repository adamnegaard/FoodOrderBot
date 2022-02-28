package dk.themacs.foodOrderBot.mail.formatters;

import dk.themacs.foodOrderBot.entities.PersonOrder;
import dk.themacs.foodOrderBot.mail.Mail;
import dk.themacs.foodOrderBot.mail.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class FoodOrderSender {

    protected final MailService mailService;
    protected final String mailFrom;
    protected final String mailCc;
    protected final String mailReceiver;
    protected final String companyName;

    private final static Logger log = LoggerFactory.getLogger(FoodOrderSender.class);
    protected final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM-yyyy");

    public FoodOrderSender(MailService mailService, String mailFrom, String mailCc, String mailReceiver, String companyName) {
        this.mailService = mailService;
        this.mailFrom = mailFrom;
        this.mailCc = mailCc;
        this.mailReceiver = mailReceiver;
        this.companyName = companyName;
    }

    public String getOnTimeOrder(Set<PersonOrder> personOrders) {
        String orderString = getOrders(personOrders);

        return getHello() + "\n" +
                getOrderRequest() + ":\n\n" +
                orderString + "\n" +
                "Tak!\n\n" +
                "Mvh\n" +
                companyName;
    }

    public String getLateOrder(Set<PersonOrder> personOrders) {
        String orderString = getOrders(personOrders);

        return getHello() + "\n" +
                getOrderModificationRequest() + ":\n\n" +
                orderString + "\n" +
                "Tak!\n\n" +
                "Mvh\n" +
                companyName;
    }

    public void orderFood(String mailContent) throws Exception {

        // include date in subject of mail
        String subject = "Madbestilling den " + LocalDate.now().format(formatter);

        Mail mail = new Mail(subject, mailFrom, companyName, mailReceiver, mailContent, mailCc);

        try {

            mailService.sendEmail(mail);

        } catch (Exception e) {

            log.error("Could not order food. Error occurred when sending the mail. Had the following mail prepared:\n" + mailContent, e);
            throw e;

        }
    }

    protected String capitalize(String s) {
        if(s == null || s.isEmpty()) {
            return "";
        }

        if(s.length() == 1) {
            return s.toUpperCase(Locale.ROOT);
        }

        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }

    protected abstract String getOrders(Set<PersonOrder> personOrders);

    protected abstract String getHello();

    protected abstract String getOrderModificationRequest();

    protected abstract String getOrderRequest();
}
