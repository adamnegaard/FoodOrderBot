package dk.themacs.foodOrderBot;

import com.slack.api.bolt.App;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SlackApp {

    private final AppConfig appConfig;

    public SlackApp(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Bean
    public App initSlackApp() {
        return new App();
    }
}