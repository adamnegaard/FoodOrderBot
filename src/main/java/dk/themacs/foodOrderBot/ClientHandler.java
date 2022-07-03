package dk.themacs.foodOrderBot;

import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.auth.AuthTestRequest;
import com.slack.api.methods.request.reactions.ReactionsAddRequest;
import com.slack.api.methods.response.auth.AuthTestResponse;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.chat.ChatUpdateResponse;
import com.slack.api.methods.response.reactions.ReactionsAddResponse;
import com.slack.api.model.Message;
import com.slack.api.model.block.ActionsBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.event.MessageEvent;
import dk.themacs.foodOrderBot.commands.CommandParser;
import dk.themacs.foodOrderBot.commands.ParsedCommand;
import dk.themacs.foodOrderBot.commands.UnknownCommandException;
import dk.themacs.foodOrderBot.config.AppConfig;
import dk.themacs.foodOrderBot.data.Result;
import dk.themacs.foodOrderBot.data.TimeUtil;
import dk.themacs.foodOrderBot.entities.BatchOrder;
import dk.themacs.foodOrderBot.entities.PersonOrder;
import dk.themacs.foodOrderBot.mail.senders.FoodOrderSender;
import dk.themacs.foodOrderBot.mail.senders.FoodOrderSenderFactory;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderService;
import dk.themacs.foodOrderBot.services.PersonOrder.PersonOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static com.slack.api.model.block.element.BlockElements.button;

@Component
public class ClientHandler {

    private final AppConfig appConfig;
    private final BatchOrderService batchOrderService;
    private final FoodOrderSenderFactory foodOrderSenderFactory;
    private final CommandParser commandParser;
    private final PersonOrderService personOrderService;

    private final static Logger log = LoggerFactory.getLogger(ClientHandler.class);

    public ClientHandler(AppConfig appConfig, BatchOrderService batchOrderService, FoodOrderSenderFactory foodOrderSenderFactory, CommandParser commandParser, PersonOrderService personOrderService) {
        this.appConfig = appConfig;
        this.batchOrderService = batchOrderService;
        this.foodOrderSenderFactory = foodOrderSenderFactory;
        this.commandParser = commandParser;
        this.personOrderService = personOrderService;
    }

    public void sendFoodOrderReminder(MethodsClient client, String text) {

        try {
            ChatPostMessageResponse messageResponse = sendMessage(client, text);
            log.info("Successfully sent out the food order reminder");

            // Insert it into the database
            BatchOrder batchOrder = new BatchOrder(messageResponse.getTs());
            Result<BatchOrder> result = batchOrderService.create(batchOrder);

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
        String orderText = messageEvent.getText();
        String eventTs = messageEvent.getEventTs();

        // ignore all messages coming from the bot itself
        if(!botUserId.equals(userId) && threadTs != null) {
            Result<BatchOrder> batchOrderResult = batchOrderService.readRecent();

            if(!batchOrderResult.isError() && threadTs.equals(batchOrderResult.getValue().getStartedTs())) {

                try {
                    ParsedCommand command = commandParser.getParsedCommand(orderText);

                    //Check if the order is late
                    boolean lateOrder = isOrderLate();

                    handleCommand(client, command, userId, channelId, threadTs, eventTs, lateOrder);

                    log.info("Processed order of user with ID: " + userId);
                } catch(UnknownCommandException unknownCommandException) {
                    try {
                        if(orderText.toLowerCase().contains("springer")) {
                            addReaction(client, "kangaroo", channelId, eventTs);
                        }

                        if (unknownCommandException.isInform()) {
                            sendMessage(client, unknownCommandException.getMessage(), threadTs);
                        }
                    } catch (Exception e) {
                        log.error("Error when handling UnknownCommandException", e);
                    }
                } catch (Exception e) {
                    log.error("Unknown error processing order of user with ID: " + userId, e);
                }
            }
        }
        return ctx.ack();
    }

    public boolean isOrderLate() {
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
            case "ekstra":
                // The guest user wil be assigned a random guid as it's user id.
                UUID randomUserId = UUID.randomUUID();
                handleOrder(client, randomUserId.toString(), threadTs, eventTs, arguments, channelId, lateOrder);
                return;
            default:
                throw new UnknownCommandException("Ukendt kommando: '" + command + "'");
        }
    }

    private void handleOrder(MethodsClient client, String userId, String threadTs, String eventTs, String arguments, String channelId, boolean lateOrder) throws SlackApiException, IOException {
        String order = trimOrder(arguments);

        PersonOrder personOrder = new PersonOrder(userId, eventTs, order);
        personOrderService.create(personOrder, threadTs);

        addReaction(client, determineEmojiFromOrder(order), channelId, eventTs);

        // Remind the person that the order was late
        if (lateOrder) {
            orderFood(client, true);
        }
    }

    private String determineEmojiFromOrder(String order) {
        String lowerCaseOrder = order.toLowerCase(Locale.ROOT);
        if (lowerCaseOrder.contains("club")) {
            return "bacon";
        } else if (lowerCaseOrder.contains("salat")) {
            return "green_salad";
        } else if (lowerCaseOrder.contains("sand")) {
            return "sandwich";
        } else if (lowerCaseOrder.contains("tun") || lowerCaseOrder.contains("laks")) {
            return "fish";
        } else {
            return "thumbsup";
        }
    }

    private String trimOrder(String order) {
        return order
                .trim()
                // Some people might write tak
                .replace("tak", "")
                // Save an order as lower case
                .toLowerCase(Locale.ROOT)
                .replaceAll("[.]", "")
                .replaceAll("…", "");
    }

    public ReactionsAddResponse addReaction(MethodsClient client, String emoji, String channelId, String eventTs) throws SlackApiException, IOException {
        return client.reactionsAdd(ReactionsAddRequest.builder()
                .name(emoji)
                .channel(channelId)
                .timestamp(eventTs)
                .build());
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

    private ChatPostMessageResponse sendFoodOrderActionMessage(MethodsClient client, String messageTitle, String foodOrderMessage, String threadTs) throws SlackApiException, IOException {

        return client.chatPostMessage(r -> r
                .channel(appConfig.getChannelId())
                .blocks(asBlocks(
                        section(section -> section.text(markdownText("*" + messageTitle + "*"))),
                        section(section -> section.text(plainText(foodOrderMessage))),
                        divider(),
                        actions(actions -> actions
                                .elements(asElements(button(b -> b.text(plainText("Bestil"))
                                        .value(foodOrderMessage)
                                        .style("primary")
                                        .actionId("order_food_action")
                                        .confirm(confirmationDialog(c -> c
                                                .title(plainText("Har alle på kontoret bestilt mad?"))
                                                .text(plainText("Er du sikker?"))
                                                .confirm(plainText("Ja"))
                                                .deny(plainText("Nej")))))))
                        )))
                .threadTs(threadTs)
                .text(messageTitle));
    }


    public void orderFood(MethodsClient client, boolean lateOrder) {

        try {
            Optional<BatchOrder> recentBatchOptional = getRecentBatchOrder();

            if(recentBatchOptional.isPresent()) {

                BatchOrder batchOrder = recentBatchOptional.get();

                boolean isLateOrder = lateOrder && batchOrder.isOrdered();

                Optional<String> foodOrderMessageOptional = getFoodOrderMessage(client, batchOrder, isLateOrder, true);

                if(foodOrderMessageOptional.isPresent()) {

                    String foodOrderMessage = foodOrderMessageOptional.get();

                    if (isLateOrder) {
                        handleOrderUpdate(client, foodOrderMessage, batchOrder);
                    } else {
                        handleOnTimeOrder(client, foodOrderMessage, batchOrder);
                    }
                }
            }

        } catch(Exception e) {
            log.error("Error ordering food", e);
        }
    }

    private void handleOnTimeOrder(MethodsClient client, String foodOrderMessage, BatchOrder batchOrder) throws SlackApiException, IOException {
        // log a confirmation
        log.info("Sent out message for batch order with id: " + batchOrder.getId());
        // send a confirmation in slack
        sendFoodOrderActionMessage(client, "Bestil dagens frokost besked", foodOrderMessage, batchOrder.getStartedTs());
    }

    private void handleOrderUpdate(MethodsClient client, String foodOrderMessage, BatchOrder batchOrder) throws SlackApiException, IOException {
        // log a confirmation
        log.info("Sent out late message for batch order with id: " + batchOrder.getId());
        // send a confirmation in slack
        sendFoodOrderActionMessage(client, "Opdater dagens frokost besked", foodOrderMessage, batchOrder.getStartedTs());
    }

    public Optional<String> getFoodOrderMessage(MethodsClient client) {

        Optional<BatchOrder> recentBatchOptional = getRecentBatchOrder();

        if(recentBatchOptional.isPresent()) {
            return getFoodOrderMessage(client, recentBatchOptional.get(), false, false);
        }

        return Optional.empty();

    }

    private Optional<BatchOrder> getRecentBatchOrder() {
        Result<BatchOrder> batchOrderResult = batchOrderService.readRecent();
        if (batchOrderResult.isError()) {
            log.info("No batch orders were started today");
            return Optional.empty();
        }
        BatchOrder batchOrder = batchOrderResult.getValue();
        return Optional.of(batchOrder);
    }

    public Optional<String> getFoodOrderMessage(MethodsClient client, BatchOrder batchOrder, boolean lateOrder, boolean inform) {
        try {

            Set<PersonOrder> personOrders = batchOrder.getPersonOrders();

            if(personOrders.isEmpty()) {
                log.info("No person orders were made on the batch for today");

                if (inform) sendMessage(client, "Ingen bestillinger var oprettet.", batchOrder.getStartedTs());
                return Optional.empty();
            }

            String mailContent;
            FoodOrderSender foodOrderSender = foodOrderSenderFactory.getFoodOrderSenderForToday();
            if(lateOrder) {
                mailContent = foodOrderSender.getLateOrder(personOrders);
            } else {
                mailContent = foodOrderSender.getOnTimeOrder(personOrders);
            }

            return Optional.of(mailContent);
        } catch (Exception e) {
            log.error("Could not send message in slack", e);
            return Optional.empty();
        }
    }

    public void orderFood(String order) throws Exception {
        Result<BatchOrder> batchOrderResult = batchOrderService.readRecent();
        if (batchOrderResult.isError()) {
            log.info("No batch orders were started today");
            return;
        }

        FoodOrderSender foodOrderSender = foodOrderSenderFactory.getFoodOrderSenderForToday();

        foodOrderSender.orderFood(order);

        BatchOrder batchOrder = batchOrderResult.getValue();
        batchOrderService.order(batchOrder.getId());
    }

    public String getBotUserId(MethodsClient client) {
        try {
            // get the bots userID
            AuthTestResponse authTestResponse = client.authTest(AuthTestRequest.builder().build());
            return authTestResponse.getUserId();
        } catch (Exception e) {
            log.error("Unable to get bots user id", e);
            return null;
        }
    }

    public void closeOrder(MethodsClient client) {
        Result<BatchOrder> batchOrderResult = batchOrderService.readRecent();
        if (batchOrderResult.isError()) {
            log.info("No batch orders were started today");
            return;
        }

        BatchOrder batchOrder = batchOrderResult.getValue();
        batchOrderService.order(batchOrder.getId());

        try {
            String botUserId = getBotUserId(client);
            removeActionsFromFoodOrderMessages(client, batchOrder.getStartedTs(), appConfig.getChannelId(), botUserId);
        } catch (Exception e) {
            log.error("Unable to remove actions from food order message when closing it", e);
        }

        log.info("Successfully closed the batch order with ID: " + batchOrder.getId());
    }

    public void removeActionsFromFoodOrderMessages(MethodsClient client, String messageThreadTs, String channelId, String userId) throws SlackApiException, IOException {
        List<Message> sendFoodOrderMessages = listSendFoodOrderMessages(client, messageThreadTs, channelId, userId);
        for(Message message : sendFoodOrderMessages) {
            removeActions(client, message, channelId);
        }
    }

    private List<Message> listSendFoodOrderMessages(MethodsClient methodsClient, String messageThreadTs, String channelId, String botUserId) throws SlackApiException, IOException {
        var repliesToFoodThread = methodsClient.conversationsReplies(c -> c
                .ts(messageThreadTs)
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
