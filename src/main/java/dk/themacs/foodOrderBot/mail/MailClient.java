package dk.themacs.foodOrderBot.mail;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MailClient implements MailService {
    private final SendGrid sendGrid;
    private final static Logger log = LoggerFactory.getLogger(MailClient.class);

    public MailClient(SendGrid sendGrid) {
        this.sendGrid = sendGrid;

    }

    @Override
    public void sendEmail(Mail mail) throws Exception {
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            if(response.getStatusCode() != 202) {
                log.error(response.getBody());
                throw new Exception("Error when sending mail, got status: " + response.getStatusCode());
            }
        } catch (IOException e) {
            log.error("Unknown error when sending mail", e);
            throw e;
        }
    }
}
