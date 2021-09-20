package dk.themacs.foodOrderBot;

import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.reactions.ReactionsAddRequest;
import com.slack.api.model.event.MessageEvent;
import dk.themacs.foodOrderBot.commands.CommandParser;
import dk.themacs.foodOrderBot.commands.ParsedCommand;
import dk.themacs.foodOrderBot.commands.UnknownCommandException;
import dk.themacs.foodOrderBot.data.Result;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderReadDTO;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderService;
import dk.themacs.foodOrderBot.services.PersonOrder.PersonOrderCreateDTO;
import dk.themacs.foodOrderBot.services.PersonOrder.PersonOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;


@Component
public class EventHandler {
    private final PersonOrderService personOrderService;
    private final BatchOrderService batchOrderService;
    private final CommandParser commandParser;

    private final static Logger log = LoggerFactory.getLogger(EventHandler.class);

    public EventHandler(PersonOrderService personOrderService, BatchOrderService batchOrderService, CommandParser commandParser) {
        this.personOrderService = personOrderService;
        this.batchOrderService = batchOrderService;
        this.commandParser = commandParser;
    }

    public Response handleOrderProcessing(MethodsClient client, String channelId, MessageEvent messageEvent, EventContext ctx) {
        String threadTs = messageEvent.getThreadTs();
        String userId = messageEvent.getUser();

        if(threadTs != null) {
            Result<BatchOrderReadDTO> batchOrderResult = batchOrderService.readRecent();

            if(!batchOrderResult.isError() && threadTs.equals(batchOrderResult.getValue().getStartedTs())) {

                try {
                    ParsedCommand command = commandParser.getParsedCommand(messageEvent.getText());
                    handleCommand(client, command, userId, channelId, threadTs, messageEvent.getEventTs());

                    log.info("Processed order of user with ID: " + userId);
                } catch(UnknownCommandException unknownCommandException) {
                    try {
                        client.chatPostMessage(r -> r
                                .channel(channelId)
                                .threadTs(threadTs)
                                .text(unknownCommandException.getMessage()));
                    } catch (Exception e) {

                    }
                } catch (Exception e) {
                    log.error("Unknown error processing order of user with ID: " + userId, e);
                }
            }
        }
        return ctx.ack();
    }

    private void handleCommand(MethodsClient client, ParsedCommand parsedCommand, String userId, String channelId, String threadTs, String eventTs) throws UnknownCommandException, SlackApiException, IOException {
        String command = parsedCommand.getCommand();
        String arguments = parsedCommand.getArguments();

        switch (command.toLowerCase(Locale.ROOT)){
            case "bestil":
                handleOrder(client, userId, threadTs, eventTs, arguments, channelId);
                return;
            case "ekstra":
                handleOrder(client, "guest", threadTs, eventTs, arguments, channelId);
                return;
            default:
                throw new UnknownCommandException("Ukendt kommando: ?" + command);
        }
    }

    private void handleOrder(MethodsClient client, String userId, String threadTs, String eventTs, String arguments, String channelId) throws SlackApiException, IOException {
        String order = trimOrder(arguments);

        PersonOrderCreateDTO personOrder = new PersonOrderCreateDTO(userId, threadTs, order);
        personOrderService.create(personOrder);

        client.reactionsAdd(ReactionsAddRequest.builder()
                .name(determineEmojiFromOrder(order))
                .channel(channelId)
                .timestamp(eventTs)
                .build());
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
