package dk.themacs.foodOrderBot.models;

import dk.themacs.foodOrderBot.entities.PersonOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonOrderRepository extends JpaRepository<PersonOrder, Long> {
    Optional<PersonOrder> findByUserIdAndBatchOrderId(String userId, long batchOrderId);
}
