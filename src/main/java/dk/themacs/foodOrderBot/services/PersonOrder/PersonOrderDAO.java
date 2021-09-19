package dk.themacs.foodOrderBot.services.PersonOrder;

import dk.themacs.foodOrderBot.data.Result;
import dk.themacs.foodOrderBot.data.Status;
import dk.themacs.foodOrderBot.entities.BatchOrder;
import dk.themacs.foodOrderBot.entities.PersonOrder;
import dk.themacs.foodOrderBot.models.BatchOrderRepository;
import dk.themacs.foodOrderBot.models.PersonOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PersonOrderDAO implements PersonOrderService {
    private final PersonOrderRepository personOrderRepository;
    private final BatchOrderRepository batchOrderRepository;

    public PersonOrderDAO(PersonOrderRepository personOrderRepository, BatchOrderRepository batchOrderRepository) {
        this.personOrderRepository = personOrderRepository;
        this.batchOrderRepository = batchOrderRepository;
    }

    @Override
    public Result<PersonOrderReadDTO> read(String userId, LocalDateTime batchOrderTs) {
        Optional<BatchOrder> batchOrderOptional = batchOrderRepository.findByStartedTs(batchOrderTs);
        if(!batchOrderOptional.isPresent()) {
            return new Result(Status.BADREQUEST, "No batch orders for user with timestamp: " + batchOrderTs);
        }
        BatchOrder batchOrder = batchOrderOptional.get();
        Optional<PersonOrder> personOrderOptional = personOrderRepository.findByUserIdAndBatchOrderId(userId, batchOrder.getId());
        if(!personOrderOptional.isPresent()) {
            return new Result(Status.BADREQUEST, "No person orders for user with ID: " + userId + " on batch order with timestamp: " + batchOrderTs);
        }
        return new Result(Status.OK, new PersonOrderReadDTO(personOrderOptional.get()));
    }

    @Override
    public Result<PersonOrderReadDTO> create(PersonOrderCreateDTO personOrderCreateDTO) {
        Optional<BatchOrder> batchOrderOptional = batchOrderRepository.findByStartedTs(personOrderCreateDTO.getBatchOrderTs());
        if(batchOrderOptional.isPresent()) {
            BatchOrder batchOrder = batchOrderOptional.get();
            PersonOrder personOrder;
            Optional<PersonOrder> existingPersonOrder = personOrderRepository.findByUserIdAndBatchOrderId(personOrderCreateDTO.getUserId(), batchOrder.getId());
            if (existingPersonOrder.isPresent()) {
                // If a person order with the same user id and batch order id, then update the order
                personOrder = existingPersonOrder.get();
                personOrder.setOrderText(personOrderCreateDTO.getOrderText());
            } else {
                // Otherwise, create a new one if it does not already exist
                personOrder = new PersonOrder(personOrderCreateDTO.getUserId(), batchOrder, personOrderCreateDTO.getOrderText());
            }

            try {
                PersonOrder createdPersonOrder = personOrderRepository.save(personOrder);
                return new Result(Status.CREATED, new PersonOrderReadDTO(createdPersonOrder));
            } catch (Exception e) {
                var a = 1;
            }
        }
        return new Result(Status.BADREQUEST, "Batch order with timestamp: " + personOrderCreateDTO.getBatchOrderTs() + " does not exist");
    }
}
