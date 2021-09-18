package dk.themacs.foodOrderBot;

import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.reactions.ReactionsAddRequest;
import com.slack.api.model.event.MessageEvent;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderService;
import dk.themacs.foodOrderBot.services.PersonOrder.PersonOrderCreateDTO;
import dk.themacs.foodOrderBot.services.PersonOrder.PersonOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Locale;


@Component
public class EventHandler {
    private final PersonOrderService personOrderService;
    private final BatchOrderService batchOrderService;

    private final static Logger log = LoggerFactory.getLogger(EventHandler.class);

    public EventHandler(PersonOrderService personOrderService, BatchOrderService batchOrderService) {
        this.personOrderService = personOrderService;
        this.batchOrderService = batchOrderService;
    }

    public Response handleOrderProcessing(MethodsClient client, String channelId, MessageEvent messageEvent, EventContext ctx) {
        String threadTs = messageEvent.getThreadTs();
        String userId = messageEvent.getUser();
        if(threadTs != null && !batchOrderService.readRecent().isError()) {

            var order = trimOrder(messageEvent.getText());

            PersonOrderCreateDTO personOrder = new PersonOrderCreateDTO(userId, LocalDate.now(), order);
            personOrderService.create(personOrder);
            try {
                client.reactionsAdd(ReactionsAddRequest.builder()
                        .name(determineEmojiFromOrder(order))
                        .channel(channelId)
                        .timestamp(messageEvent.getEventTs())
                        .build());

                log.debug("Processed order of user with ID: " + userId);
            } catch (Exception e) {
                log.error("Unknown error processing order of user with ID: " + userId, e);
            }
        }
        return ctx.ack();
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
}
