package dk.themacs.foodOrderBot.services.BatchOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BatchOrderCreateDTO {
    private LocalDate startedTs;

    public BatchOrderCreateDTO() {}

    public BatchOrderCreateDTO(LocalDate startedTs) {
        this.startedTs = startedTs;
    }

    public LocalDate getStartedTs() {
        return startedTs;
    }

    public void setStartedTs(LocalDate startedTs) {
        this.startedTs = startedTs;
    }
}
