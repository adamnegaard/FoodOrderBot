package dk.themacs.foodOrderBot.services.PersonOrder;

import dk.themacs.foodOrderBot.entities.BatchOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PersonOrderCreateDTO {
    private String userId;
    private String batchOrderTs;
    private String orderText;

    public PersonOrderCreateDTO() {}

    public PersonOrderCreateDTO(String userId, String batchOrderTs, String orderText) {
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

    public String getBatchOrderTs() {
        return batchOrderTs;
    }

    public void setBatchOrderTs(String batchOrderTs) {
        this.batchOrderTs = batchOrderTs;
    }

    public String getOrderText() {
        return orderText;
    }

    public void setOrderText(String orderText) {
        this.orderText = orderText;
    }
}
