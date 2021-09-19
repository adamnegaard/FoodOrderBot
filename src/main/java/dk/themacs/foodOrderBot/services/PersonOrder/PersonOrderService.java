package dk.themacs.foodOrderBot.services.PersonOrder;

import dk.themacs.foodOrderBot.data.Result;

import java.time.LocalDateTime;

public interface PersonOrderService {
    Result<PersonOrderReadDTO> read(String userId, LocalDateTime batchOrderTs);
    Result<PersonOrderReadDTO> create(PersonOrderCreateDTO personOrderCreateDTO);
}
