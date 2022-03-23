package dk.themacs.foodOrderBot.config;

import com.slack.api.Slack;
import com.slack.api.bolt.App;
import com.slack.api.methods.MethodsClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SlackApp {

    @Bean
    public App initSlackApp() {

        return new App();
    }

}