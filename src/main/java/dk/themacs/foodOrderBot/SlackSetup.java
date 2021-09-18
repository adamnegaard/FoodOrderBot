package dk.themacs.foodOrderBot;

import com.slack.api.Slack;
import com.slack.api.bolt.App;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.methods.MethodsClient;
import com.slack.api.model.event.MessageEvent;
import com.slack.api.socket_mode.SocketModeClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackSetup {

    private final AppConfig appConfig;
    private final EventHandler eventHandler;
    private final App app;

    public SlackSetup(AppConfig appConfig, EventHandler eventHandler, App app) {
        this.appConfig = appConfig;
        this.eventHandler = eventHandler;
        this.app = app;
    }

    @Bean
    public MethodsClient initSlackClient() {
        Slack slack = app.getSlack();
        MethodsClient methodsClient = slack.methods(appConfig.getBotUserOAuthToken());
        try {
            app.event(MessageEvent.class, (payload, ctx) -> eventHandler.handleOrderProcessing(methodsClient, appConfig.getChannelId(), payload.getEvent(), ctx));

            SocketModeApp socketModeApp = new SocketModeApp(
                    appConfig.getBotAppToken(),
                    SocketModeClient.Backend.JavaWebSocket,
                    app
            );
            socketModeApp.startAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return methodsClient;
    }


}
