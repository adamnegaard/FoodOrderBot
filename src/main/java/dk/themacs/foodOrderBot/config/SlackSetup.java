package dk.themacs.foodOrderBot.config;

import com.slack.api.Slack;
import com.slack.api.bolt.App;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.auth.AuthTestRequest;
import com.slack.api.methods.response.auth.AuthTestResponse;
import com.slack.api.model.event.MessageEvent;
import com.slack.api.socket_mode.SocketModeClient;
import dk.themacs.foodOrderBot.ClientHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Optional;

@Configuration
public class SlackSetup {

    private final AppConfig appConfig;
    private final ClientHandler clientHandler;
    private final App app;

    public SlackSetup(AppConfig appConfig, ClientHandler clientHandler, App app) {
        this.appConfig = appConfig;
        this.clientHandler = clientHandler;
        this.app = app;
    }

    @Bean
    public MethodsClient initSlackClient() throws SlackApiException, IOException {
        Slack slack = app.getSlack();
        MethodsClient methodsClient = slack.methods(appConfig.getBotUserOAuthToken());

        //Get the bots UserID
        AuthTestResponse authTestResponse = methodsClient.authTest(AuthTestRequest.builder().build());
        String userId = authTestResponse.getUserId();

        try {

            app.command("/ping", (req, ctx) -> ctx.ack("pong"));

            app.command("/ordre", (req, ctx) -> {

                Optional<String> orderMessageOptional = clientHandler.getFoodOrderMessage(methodsClient);
                if(orderMessageOptional.isPresent()) {
                    return ctx.ack(orderMessageOptional.get());
                }
                else return ctx.ack("Ingen åbne bestillinger.");
            });

            app.event(MessageEvent.class, (payload, ctx) -> clientHandler.handleOrderProcessing(methodsClient, appConfig.getChannelId(), userId, payload.getEvent(), ctx));

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
