package dk.themacs.foodOrderBot.services.BatchOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BatchOrderCreateDTO {
    private String startedTs;

    public BatchOrderCreateDTO() {}

    public BatchOrderCreateDTO(String startedTs) {
        this.startedTs = startedTs;
    }

    public String getStartedTs() {
        return startedTs;
    }

    public void setStartedTs(String startedTs) {
        this.startedTs = startedTs;
    }
}
