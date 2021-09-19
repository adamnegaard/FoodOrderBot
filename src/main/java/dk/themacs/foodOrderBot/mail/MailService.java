package dk.themacs.foodOrderBot.mail;

import javax.mail.MessagingException;

public interface MailService {
    public void sendEmail(Mail mail) throws MessagingException, Exception;
}
