package dk.themacs.foodOrderBot.services.BatchOrder;

import dk.themacs.foodOrderBot.data.Result;
import dk.themacs.foodOrderBot.data.Status;
import dk.themacs.foodOrderBot.entities.BatchOrder;
import dk.themacs.foodOrderBot.models.BatchOrderRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class BatchOrderDAO implements BatchOrderService {
    private final BatchOrderRepository batchOrderRepository;

    public BatchOrderDAO(BatchOrderRepository batchOrderRepository) {
        this.batchOrderRepository = batchOrderRepository;
    }

    @Override
    public Collection<BatchOrderReadDTO> read() {
        Iterable<BatchOrder> batchOrders = batchOrderRepository.findAll();
        return StreamSupport.stream(batchOrders.spliterator(), false)
                .map(batchOrder -> new BatchOrderReadDTO(batchOrder))
                .collect(Collectors.toList());
    }

    @Override
    public Result<BatchOrderReadDTO> readRecent() {
        Iterable<BatchOrder> batchOrders = batchOrderRepository.findAll();
        Optional<BatchOrder> batchOrderOptional = StreamSupport.stream(batchOrders.spliterator(), false)
                .filter(bo -> !bo.isOrdered())
                .max(Comparator.comparing(BatchOrder::getStartedTs));

        if(!batchOrderOptional.isPresent()) {
            return new Result(Status.BADREQUEST, "No batch orders that are not ordered yet");
        }

        return new Result(Status.OK, new BatchOrderReadDTO(batchOrderOptional.get()));
    }

    @Override
    public Result<BatchOrderReadDTO> order(long batchOrderId) {
        Optional<BatchOrder> batchOrderOptional = batchOrderRepository.findById(batchOrderId);
        if(batchOrderOptional.isPresent()) {
            BatchOrder existingBatchOrder = batchOrderOptional.get();
            existingBatchOrder.setOrdered(true);
            BatchOrder updatedBatchOrder = batchOrderRepository.save(existingBatchOrder);
            return new Result(Status.OK, new BatchOrderReadDTO(updatedBatchOrder));
        }
        return new Result(Status.BADREQUEST, "No batch orders with ID: " + batchOrderId);
    }

    @Override
    public Result<BatchOrderReadDTO> create(BatchOrderCreateDTO batchOrderDTO) {
        try {
            BatchOrder batchOrderCreate = new BatchOrder(batchOrderDTO.getStartedTs());
            BatchOrder batchOrder = batchOrderRepository.save(batchOrderCreate);
            return new Result(Status.CREATED, new BatchOrderReadDTO(batchOrder));
        } catch (Exception e) {
            return new Result(Status.BADREQUEST, "Could not create the batch order with timestamp: " + batchOrderDTO.getStartedTs());
        }
    }
}
