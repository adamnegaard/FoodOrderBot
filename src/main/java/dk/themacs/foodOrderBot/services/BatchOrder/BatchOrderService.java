package dk.themacs.foodOrderBot.services.BatchOrder;

import dk.themacs.foodOrderBot.data.Result;
import dk.themacs.foodOrderBot.entities.BatchOrder;

import java.util.Collection;

public interface BatchOrderService {
    Collection<BatchOrder> read();
    Result<BatchOrder> readRecent();
    Result<BatchOrder> order(long batchOrderId);
    Result<BatchOrder> create(BatchOrder batchOrderCreate);
}
