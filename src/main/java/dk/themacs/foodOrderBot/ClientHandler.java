package dk.themacs.foodOrderBot;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import dk.themacs.foodOrderBot.data.Result;
import dk.themacs.foodOrderBot.data.TimeUtil;
import dk.themacs.foodOrderBot.mail.FoodOrderSender;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderCreateDTO;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderReadDTO;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderService;
import dk.themacs.foodOrderBot.services.PersonOrder.PersonOrderReadDTO;
import dk.themacs.foodOrderBot.services.PersonOrder.PersonOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
public class ClientHandler {
    private final MethodsClient client;
    private final BatchOrderService batchOrderService;
    private final PersonOrderService personOrderService;
    private final FoodOrderSender foodOrderSender;

    private final static Logger log = LoggerFactory.getLogger(ClientHandler.class);

    public ClientHandler(MethodsClient client, BatchOrderService batchOrderService, PersonOrderService personOrderService, FoodOrderSender foodOrderSender) {
        this.client = client;
        this.batchOrderService = batchOrderService;
        this.personOrderService = personOrderService;
        this.foodOrderSender = foodOrderSender;

        sendFoodOrderMessage("Hvad kunne du tænke dig til frokost på kontoret fra baksandwich.dk i dag?\n" +
                "Bestilling skal sendes til kontakt@baksandwich.dk inden kl. 9:30 I DAG.");
    }

    public void sendFoodOrderMessage(String text) {

        try {
            ChatPostMessageResponse messageResponse = client.chatPostMessage(r -> r.channel("C02EWJFBMNX").text(text));

            LocalDateTime timeStamp = TimeUtil.getDateTimeFromStringInSeconds(messageResponse.getTs());
            // Insert it into the database
            BatchOrderCreateDTO batchOrder = new BatchOrderCreateDTO(timeStamp);
            Result<BatchOrderReadDTO> result = batchOrderService.create(batchOrder);
            if(!result.isError()) {
                log.debug("Created the batch order in the database at " + timeStamp);
            } else {
                log.error(result.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("Could not sent daily message to order food", e);
        }
    }

    public void orderFood() {
        try {
            Result<BatchOrderReadDTO> batchOrderResult = batchOrderService.readRecent();
            if (batchOrderResult.isError()) {
                log.debug("No batch orders were started today");
                return;
            }
            BatchOrderReadDTO batchOrder = batchOrderResult.getValue();

            Set<PersonOrderReadDTO> personOrders = batchOrder.getPersonOrders();

            if(personOrders.isEmpty()) {
                log.debug("No person orders were made on the batch for today");
                return;
            }
            foodOrderSender.orderFood(batchOrder.getPersonOrders());

            // TODO: If everything was successful end the batch order
        } catch (Exception e) {
            log.error("Could not order the food", e);
        }
    }
}
