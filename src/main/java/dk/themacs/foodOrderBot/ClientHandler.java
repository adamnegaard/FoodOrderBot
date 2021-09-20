package dk.themacs.foodOrderBot;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import dk.themacs.foodOrderBot.config.AppConfig;
import dk.themacs.foodOrderBot.data.Result;
import dk.themacs.foodOrderBot.data.TimeUtil;
import dk.themacs.foodOrderBot.mail.FoodOrderSender;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderCreateDTO;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderReadDTO;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderService;
import dk.themacs.foodOrderBot.services.PersonOrder.PersonOrderReadDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class ClientHandler {
    private final MethodsClient client;
    private final BatchOrderService batchOrderService;
    private final AppConfig appConfig;
    private final FoodOrderSender foodOrderSender;

    private final static Logger log = LoggerFactory.getLogger(ClientHandler.class);

    public ClientHandler(MethodsClient client, BatchOrderService batchOrderService, AppConfig appConfig, FoodOrderSender foodOrderSender) {
        this.client = client;
        this.batchOrderService = batchOrderService;
        this.appConfig = appConfig;
        this.foodOrderSender = foodOrderSender;
    }

    public void sendFoodOrderReminder(String text) {

        try {
            ChatPostMessageResponse messageResponse = sendMessage(text);
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

    private ChatPostMessageResponse sendMessage(String text) throws SlackApiException, IOException {
        return sendMessage(text,null);
    }

    private ChatPostMessageResponse sendMessage(String text, String threadTs) throws SlackApiException, IOException {
        return client.chatPostMessage(r -> r
                .channel(appConfig.getChannelId())
                .threadTs(threadTs)
                .text(text));
    }

    public void orderFood() {
        try {
            Result<BatchOrderReadDTO> batchOrderResult = batchOrderService.readRecent();
            if (batchOrderResult.isError()) {
                log.info("No batch orders were started today");
                return;
            }
            BatchOrderReadDTO batchOrder = batchOrderResult.getValue();

            Set<PersonOrderReadDTO> personOrders = batchOrder.getPersonOrders();

            if(personOrders.isEmpty()) {
                log.info("No person orders were made on the batch for today");
                sendMessage("Ingen bestillinger var oprettet, sender ikke en ordre.", batchOrder.getStartedTs());
                return;
            }
            // send the email to the restaurant
            foodOrderSender.orderFood(batchOrder.getPersonOrders());

            // close the order
            batchOrderService.order(batchOrder.getId());

            // send a confirmation in slack
            log.info("Successfully closed the batch order with ID: " + batchOrder.getId());
            sendMessage("Bestillingen er sendt!", batchOrder.getStartedTs());
        } catch (Exception e) {
            log.error("Could not order the food", e);
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
