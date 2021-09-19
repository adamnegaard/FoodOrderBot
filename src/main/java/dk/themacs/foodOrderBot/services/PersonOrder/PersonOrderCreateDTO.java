package dk.themacs.foodOrderBot.services.PersonOrder;

import dk.themacs.foodOrderBot.entities.BatchOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PersonOrderCreateDTO {
    private String userId;
    private LocalDateTime batchOrderTs;
    private String orderText;

    public PersonOrderCreateDTO() {}

    public PersonOrderCreateDTO(String userId, LocalDateTime batchOrderTs, String orderText) {
        this.userId = userId;
        this.batchOrderTs = batchOrderTs;
        this.orderText = orderText;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getBatchOrderTs() {
        return batchOrderTs;
    }

    public void setBatchOrderTs(LocalDateTime batchOrderTs) {
        this.batchOrderTs = batchOrderTs;
    }

    public String getOrderText() {
        return orderText;
    }

    public void setOrderText(String orderText) {
        this.orderText = orderText;
    }
}
