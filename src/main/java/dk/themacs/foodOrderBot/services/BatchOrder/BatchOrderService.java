package dk.themacs.foodOrderBot.services.BatchOrder;

import dk.themacs.foodOrderBot.Result;

import java.util.Collection;

public interface BatchOrderService {
    Collection<BatchOrderReadDTO> read();
    Result<BatchOrderReadDTO> readRecent();
    Result<BatchOrderReadDTO> order(long batchOrderId);
    Result<BatchOrderReadDTO> create(BatchOrderCreateDTO batchOrderDTO);
}
