package dk.themacs.foodOrderBot;

import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.reactions.ReactionsAddRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.event.MessageEvent;
import dk.themacs.foodOrderBot.commands.CommandParser;
import dk.themacs.foodOrderBot.commands.ParsedCommand;
import dk.themacs.foodOrderBot.commands.UnknownCommandException;
import dk.themacs.foodOrderBot.config.AppConfig;
import dk.themacs.foodOrderBot.data.Result;
import dk.themacs.foodOrderBot.data.TimeUtil;
import dk.themacs.foodOrderBot.mail.FoodOrderSender;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderCreateDTO;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderReadDTO;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderService;
import dk.themacs.foodOrderBot.services.PersonOrder.PersonOrderCreateDTO;
import dk.themacs.foodOrderBot.services.PersonOrder.PersonOrderReadDTO;
import dk.themacs.foodOrderBot.services.PersonOrder.PersonOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Component
public class ClientHandler {
    private final AppConfig appConfig;
    private final BatchOrderService batchOrderService;
    private final FoodOrderSender foodOrderSender;
    private final CommandParser commandParser;
    private final PersonOrderService personOrderService;

    private final static Logger log = LoggerFactory.getLogger(ClientHandler.class);

    public ClientHandler(AppConfig appConfig, BatchOrderService batchOrderService, FoodOrderSender foodOrderSender, CommandParser commandParser, PersonOrderService personOrderService) {
        this.appConfig = appConfig;
        this.batchOrderService = batchOrderService;
        this.foodOrderSender = foodOrderSender;
        this.commandParser = commandParser;
        this.personOrderService = personOrderService;
    }


    public void sendFoodOrderReminder(MethodsClient client, String text) {

        try {
            ChatPostMessageResponse messageResponse = sendMessage(client, text);
            log.info("Successfully sent out the food order reminder");

            // Insert it into the database
            BatchOrderCreateDTO batchOrder = new BatchOrderCreateDTO(messageResponse.getTs());
            Result<BatchOrderReadDTO> result = batchOrderService.create(batchOrder);
            if(!result.isError()) {
                log.info("Created the batch order in the database at " + TimeUtil.getDateTimeFromStringInSeconds(messageResponse.getTs()));
            } else {
                log.error(result.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("Could not sent daily message to order food", e);
        }
    }

    public Response handleOrderProcessing(MethodsClient client, String channelId, String botUserId, MessageEvent messageEvent, EventContext ctx) {
        String threadTs = messageEvent.getThreadTs();
        String userId = messageEvent.getUser();

        // ignore all messages coming from the bot itself
        if(!botUserId.equals(userId) && threadTs != null) {
            Result<BatchOrderReadDTO> batchOrderResult = batchOrderService.readRecent();

            if(!batchOrderResult.isError() && threadTs.equals(batchOrderResult.getValue().getStartedTs())) {

                try {
                    ParsedCommand command = commandParser.getParsedCommand(messageEvent.getText());

                    //Check if the order is late
                    boolean lateOrder = isOrderLate();

                    handleCommand(client, command, userId, channelId, threadTs, messageEvent.getEventTs(), lateOrder);

                    log.info("Processed order of user with ID: " + userId);
                } catch(UnknownCommandException unknownCommandException) {
                    try {
                        sendMessage(client, unknownCommandException.getMessage(), threadTs);
                    } catch (Exception e) {

                    }
                } catch (Exception e) {
                    log.error("Unknown error processing order of user with ID: " + userId, e);
                }
            }
        }
        return ctx.ack();
    }

    public boolean isOrderLate(){
        LocalDateTime now = LocalDateTime.now();
        return now.getHour() >= AppConfig.orderHour && now.getMinute() >= AppConfig.orderMinute;
    }

    private void handleCommand(MethodsClient client, ParsedCommand parsedCommand, String userId, String channelId, String threadTs, String eventTs, boolean lateOrder) throws UnknownCommandException, SlackApiException, IOException {
        String command = parsedCommand.getCommand();
        String arguments = parsedCommand.getArguments();

        switch (command.toLowerCase(Locale.ROOT)){
            case "bestil":
                handleOrder(client, userId, threadTs, eventTs, arguments, channelId, lateOrder);
                return;
            //TODO: not more than one people can order as guests
            case "ekstra":
                handleOrder(client, "guest", threadTs, eventTs, arguments, channelId, lateOrder);
                return;
            default:
                throw new UnknownCommandException("Ukendt kommando: '" + command + "'");
        }
    }

    private void handleOrder(MethodsClient client, String userId, String threadTs, String eventTs, String arguments, String channelId, boolean lateOrder) throws SlackApiException, IOException {
        String order = trimOrder(arguments);

        PersonOrderCreateDTO personOrder = new PersonOrderCreateDTO(userId, threadTs, order);
        personOrderService.create(personOrder);

        client.reactionsAdd(ReactionsAddRequest.builder()
                .name(determineEmojiFromOrder(order))
                .channel(channelId)
                .timestamp(eventTs)
                .build());

        // Remind the person that the order was late
        if (lateOrder) {
            sendMessage(client, "Ordren bliver opdateret, selvom du bestilte for sent! Husk deadline på " + AppConfig.orderHour + ":" + AppConfig.orderMinute, threadTs);
            orderFood(client, lateOrder);
        }
    }

    private String determineEmojiFromOrder(String order) {
        String lowerCaseOrder = order.toLowerCase(Locale.ROOT);
        if (lowerCaseOrder.contains("salat")) {
            return "green_salad";
        } else if (lowerCaseOrder.contains("sand")) {
            return "sandwich";
        } else {
            return "thumbsup";
        }
    }

    private String trimOrder(String order) {
        return order
                // Some people might write tak
                .replace("tak", "")
                .trim();
    }

    private ChatPostMessageResponse sendMessage(MethodsClient client, String text) throws SlackApiException, IOException {
        return sendMessage(client, text,null);
    }

    private ChatPostMessageResponse sendMessage(MethodsClient client, String text, String threadTs) throws SlackApiException, IOException {
        return client.chatPostMessage(r -> r
                .channel(appConfig.getChannelId())
                .threadTs(threadTs)
                .text(text));
    }

    public void orderFood(MethodsClient client, boolean lateOrder) {
        try {
            Optional<BatchOrderReadDTO> recentBatchOptional = getRecentBatchOrder();
            if(recentBatchOptional.isPresent()) {
                BatchOrderReadDTO batchOrder = recentBatchOptional.get();
                Optional<String> foodOrderMessageOptional = getFoodOrderMessage(client, batchOrder, true);
                if(foodOrderMessageOptional.isPresent()) {
                    // send the email to the restaurant
                    foodOrderSender.orderFood(batchOrder.getPersonOrders(), lateOrder, foodOrderMessageOptional.get());

                    // send a confirmation in slack
                    log.info("Successfully ordered the batch order with ID: " + batchOrder.getId());

                    //Send a confirmation in slack if its not a delayed message
                    if (!lateOrder) sendMessage(client, "Bestillingen er sendt!", batchOrder.getStartedTs());
                }
            }
        } catch(Exception e) {
            log.error("Error ordering food", e);
        }
    }

    public Optional<String> getFoodOrderMessage(MethodsClient client) {
        Optional<BatchOrderReadDTO> recentBatchOptional = getRecentBatchOrder();
        if(recentBatchOptional.isPresent()) {
            return getFoodOrderMessage(client, recentBatchOptional.get(), false);
        }
        return Optional.empty();
    }

    private Optional<BatchOrderReadDTO> getRecentBatchOrder() {
        Result<BatchOrderReadDTO> batchOrderResult = batchOrderService.readRecent();
        if (batchOrderResult.isError()) {
            log.info("No batch orders were started today");
            return Optional.empty();
        }
        BatchOrderReadDTO batchOrder = batchOrderResult.getValue();
        return Optional.of(batchOrder);
    }

    public Optional<String> getFoodOrderMessage(MethodsClient client, BatchOrderReadDTO batchOrder, boolean inform) {
        try {
            Set<PersonOrderReadDTO> personOrders = batchOrder.getPersonOrders();

            if(personOrders.isEmpty()) {
                log.info("No person orders were made on the batch for today");
                if (inform) sendMessage(client, "Ingen bestillinger var oprettet, sender ikke en ordre.", batchOrder.getStartedTs());
                return Optional.empty();
            }

            String mailContent = foodOrderSender.mailContent(personOrders, false);
            return Optional.of(mailContent);
        } catch (Exception e) {
            log.error("Could not send message in slack", e);
            return Optional.empty();
        }
    }

    public void closeOrder() {
        Result<BatchOrderReadDTO> batchOrderResult = batchOrderService.readRecent();
        if (batchOrderResult.isError()) {
            log.info("No batch orders were started today");
            return;
        }
        BatchOrderReadDTO batchOrder = batchOrderResult.getValue();
        batchOrderService.order(batchOrder.getId());
        log.info("Successfully closed the batch order with ID: " + batchOrder.getId());
    }


}
