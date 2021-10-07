package dk.themacs.foodOrderBot.mail;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfiguration {

    private final String sendgridApiKey;

    public MailConfiguration(@Value("${sendgrid.api-key}") String sendgridApiKey) {
        this.sendgridApiKey = sendgridApiKey;
    }

    @Bean
    public SendGrid getSendGrid() {
        return new SendGrid(sendgridApiKey);
    }
}