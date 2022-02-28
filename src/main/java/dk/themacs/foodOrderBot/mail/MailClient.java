package dk.themacs.foodOrderBot.mail;

import dk.themacs.foodOrderBot.ClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
public class MailClient implements MailService {

    private final JavaMailSender mailSender;

    public MailClient(JavaMailSender mailSender) {

        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(Mail mail) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

        mimeMessageHelper.setSubject(mail.getMailSubject());
        mimeMessageHelper.setFrom(new InternetAddress(mail.getMailFrom(), mail.getMailFromName()));
        if (mail.getMailCc() != null) mimeMessageHelper.setCc(mail.getMailCc());
        mimeMessageHelper.setTo(mail.getMailTo());
        mimeMessageHelper.setText(mail.getMailContent());
        if(mail.getMailCc() != null) mimeMessageHelper.setReplyTo(mail.getMailCc());

        mailSender.send(mimeMessageHelper.getMimeMessage());
    }
}
