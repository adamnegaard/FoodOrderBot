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
    public Result<PersonOrder> read(String userId, String batchOrderTs) {
        Optional<BatchOrder> batchOrderOptional = batchOrderRepository.findByStartedTs(batchOrderTs);
        if(!batchOrderOptional.isPresent()) {
            return new Result(Status.BADREQUEST, "No batch orders for user with timestamp: " + batchOrderTs);
        }
        BatchOrder batchOrder = batchOrderOptional.get();
        Optional<PersonOrder> personOrderOptional = personOrderRepository.findByUserIdAndBatchOrderId(userId, batchOrder.getId());
        if(!personOrderOptional.isPresent()) {
            return new Result(Status.BADREQUEST, "No person orders for user with ID: " + userId + " on batch order with timestamp: " + batchOrderTs);
        }
        return new Result(Status.OK, personOrderOptional.get());
    }

    @Override
    public Result<PersonOrder> create(PersonOrder personOrderCreate, String batchOrderTs) {
        Optional<BatchOrder> batchOrderOptional = batchOrderRepository.findByStartedTs(batchOrderTs);

        if(batchOrderOptional.isPresent()) {
            BatchOrder batchOrder = batchOrderOptional.get();
            PersonOrder personOrder;
            Optional<PersonOrder> existingPersonOrder = personOrderRepository.findByUserIdAndBatchOrderId(personOrderCreate.getUserId(), batchOrder.getId());
            if (existingPersonOrder.isPresent()) {
                // If a person order with the same user id and batch order id, then update the order
                personOrder = existingPersonOrder.get();
                personOrder.setOrderTs(personOrderCreate.getOrderTs());
                personOrder.setOrderText(personOrderCreate.getOrderText());
            } else {
                // Otherwise, create a new one if it does not already exist
                personOrder = new PersonOrder(personOrderCreate.getUserId(), personOrderCreate.getOrderTs(), batchOrder, personOrderCreate.getOrderText());
            }
            PersonOrder createdPersonOrder = personOrderRepository.save(personOrder);
            return new Result(Status.CREATED, createdPersonOrder);
        }

        return new Result(Status.BADREQUEST, "Batch order with timestamp: " + batchOrderTs + " does not exist");
    }
}
