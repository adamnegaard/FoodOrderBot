package dk.themacs.foodOrderBot.services.BatchOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BatchOrderCreateDTO {
    private LocalDateTime startedTs;

    public BatchOrderCreateDTO() {}

    public BatchOrderCreateDTO(LocalDateTime startedTs) {
        this.startedTs = startedTs;
    }

    public LocalDateTime getStartedTs() {
        return startedTs;
    }

    public void setStartedTs(LocalDateTime startedTs) {
        this.startedTs = startedTs;
    }
}
