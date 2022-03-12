package dk.themacs.foodOrderBot.config;

import com.slack.api.Slack;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.auth.AuthTestRequest;
import com.slack.api.methods.response.auth.AuthTestResponse;
import com.slack.api.methods.response.chat.ChatUpdateResponse;
import com.slack.api.model.Message;
import com.slack.api.model.block.ActionsBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.event.MessageEvent;
import com.slack.api.socket_mode.SocketModeClient;
import dk.themacs.foodOrderBot.ClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
public class SlackSetup {

    private final AppConfig appConfig;
    private final ClientHandler clientHandler;
    private final App app;

    private final static Logger log = LoggerFactory.getLogger(SlackSetup.class);

    public SlackSetup(AppConfig appConfig, ClientHandler clientHandler, App app) {
        this.appConfig = appConfig;
        this.clientHandler = clientHandler;
        this.app = app;
    }

    @Bean
    public MethodsClient initSlackClient() throws SlackApiException, IOException {
        Slack slack = app.getSlack();
        MethodsClient methodsClient = slack.methods(appConfig.getBotUserOAuthToken());

        // get the bots userID
        AuthTestResponse authTestResponse = methodsClient.authTest(AuthTestRequest.builder().build());
        String userId = authTestResponse.getUserId();

        try {

            app.command("/ping", (req, ctx) -> ctx.ack("pong"));

            app.command("/ordre", (req, ctx) -> {

                Optional<String> orderMessageOptional = clientHandler.getFoodOrderMessage(methodsClient);
                String orderMessage = orderMessageOptional.orElse("Ingen åbne bestillinger.");

                return ctx.ack(orderMessage);

            });

            app.event(MessageEvent.class, (req, ctx) ->  {

                clientHandler.handleOrderProcessing(methodsClient, appConfig.getChannelId(), userId, req.getEvent(), ctx);

                return ctx.ack();

            });

            // when a user clicks a button in the actions block
            app.blockAction("order_food_action", (req, ctx) -> {

                BlockActionPayload payLoad = req.getPayload();
                BlockActionPayload.User user = payLoad.getUser();

                try {

                    List<BlockActionPayload.Action> actions = payLoad.getActions();

                    String foodOrderMessage = actions.get(0).getValue();

                    // now order the food
                    clientHandler.orderFood(foodOrderMessage);

                    log.info("User with name " + user.getName() + " successfully ordered food");

                    // react with a checkmark on the reminder and the action
                    Message actionMessage = payLoad.getMessage();
                    String channelId = payLoad.getChannel().getId();

                    clientHandler.addReaction(methodsClient, "white_check_mark", channelId, actionMessage.getThreadTs());
                    clientHandler.addReaction(methodsClient, "white_check_mark", channelId, actionMessage.getTs());

                    // remove the actions from all the previous food order messages since it should not be ordered again
                    List<Message> sendFoodOrderMessages = listSendFoodOrderMessages(methodsClient, actionMessage, channelId, userId);
                    for(Message message : sendFoodOrderMessages) {
                        removeActions(methodsClient, message, channelId);
                    }


                } catch (Exception e) {

                    log.error("Error when user with name " + user.getName() + " ordered food", e);

                }

                return ctx.ack();

            });

            // socket mode for the app
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

    private List<Message> listSendFoodOrderMessages(MethodsClient methodsClient, Message actionMessage, String channelId, String botUserId) throws SlackApiException, IOException {
        var repliesToFoodThread = methodsClient.conversationsReplies(c -> c
                .ts(actionMessage.getThreadTs())
                .channel(channelId));

        // return all the replies to the food thread sent by the bot
        return repliesToFoodThread.getMessages().stream().filter(m -> m.getUser().equals(botUserId)).collect(Collectors.toList());
    }

    private ChatUpdateResponse removeActions(MethodsClient methodsClient, Message actionMessage, String channelId) throws SlackApiException, IOException {
        if (actionMessage.getBlocks() == null) {
            return null;
        }
        // get all blocks that are not Actions
        List<LayoutBlock> blocks = actionMessage.getBlocks().stream().filter(b -> !(b instanceof ActionsBlock)).collect(Collectors.toList());

        return methodsClient.chatUpdate(c -> c
                .channel(channelId)
                .blocks(blocks)
                .ts(actionMessage.getTs())
                .text(actionMessage.getText()));
    }


}
