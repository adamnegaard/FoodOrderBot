package dk.themacs.foodOrderBot.mail;

import com.sendgrid.helpers.mail.Mail;

import javax.mail.MessagingException;

public interface MailService {
    void sendEmail(Mail mail) throws Exception;
}
