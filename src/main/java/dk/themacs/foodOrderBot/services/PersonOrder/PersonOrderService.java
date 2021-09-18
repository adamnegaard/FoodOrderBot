package dk.themacs.foodOrderBot.services.PersonOrder;

import dk.themacs.foodOrderBot.Result;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderCreateDTO;
import dk.themacs.foodOrderBot.services.BatchOrder.BatchOrderReadDTO;

import java.time.LocalDate;
import java.util.Collection;

public interface PersonOrderService {
    Result<PersonOrderReadDTO> read(String userId, LocalDate batchOrderTs);
    Result<PersonOrderReadDTO> create(PersonOrderCreateDTO personOrderCreateDTO);
}
