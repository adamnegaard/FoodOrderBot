package dk.themacs.foodOrderBot;

import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.reactions.ReactionsAddRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.event.MessageEvent;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderCreateDTO;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderReadDTO;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderService;
import dk.themacs.foodOrderBot.services.PersonOrder.PersonOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class ClientHandler {
    private final MethodsClient client;
    private final BatchOrderService batchOrderService;
    private final PersonOrderService personOrderService;

    private final static Logger log = LoggerFactory.getLogger(ClientHandler.class);

    public ClientHandler(MethodsClient client, BatchOrderService batchOrderService, PersonOrderService personOrderService) {
        this.client = client;
        this.batchOrderService = batchOrderService;
        this.personOrderService = personOrderService;

        sendFoodOrderMessage("Hvad kunne du tænke dig til frokost på kontoret fra baksandwich.dk i dag?\n" +
                "Bestilling skal sendes til kontakt@baksandwich.dk inden kl. 9:30 I DAG.");
    }

    public void sendFoodOrderMessage(String text) {

        try {
            ChatPostMessageResponse messageResponse = client.chatPostMessage(r -> r.channel("C02EWJFBMNX").text(text));

            // Insert it into the database
            LocalDate timeStamp = TimeUtil.getDateTimeFromStringInSeconds(messageResponse.getTs());
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


}
