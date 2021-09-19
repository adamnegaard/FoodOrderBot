package dk.themacs.foodOrderBot.models;

import dk.themacs.foodOrderBot.entities.BatchOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BatchOrderRepository extends JpaRepository<BatchOrder, Long> {
    Optional<BatchOrder> findByStartedTs(String startedTs);
}
