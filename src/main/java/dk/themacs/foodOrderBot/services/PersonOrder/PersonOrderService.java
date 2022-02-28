package dk.themacs.foodOrderBot.services.PersonOrder;

import dk.themacs.foodOrderBot.data.Result;
import dk.themacs.foodOrderBot.entities.PersonOrder;

import java.time.LocalDateTime;

public interface PersonOrderService {
    Result<PersonOrder> read(String userId, String batchOrderTs);
    Result<PersonOrder> create(PersonOrder personOrderCreate, String batchOrderTs);
}
