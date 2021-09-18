package dk.themacs.foodOrderBot.services.PersonOrder;

import dk.themacs.foodOrderBot.entities.BatchOrder;

import java.time.LocalDate;

public class PersonOrderCreateDTO {
    private String userId;
    private LocalDate batchOrderTs;
    private String orderText;

    public PersonOrderCreateDTO() {}

    public PersonOrderCreateDTO(String userId, LocalDate batchOrderTs, String orderText) {
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

    public LocalDate getBatchOrderTs() {
        return batchOrderTs;
    }

    public void setBatchOrderTs(LocalDate batchOrderTs) {
        this.batchOrderTs = batchOrderTs;
    }

    public String getOrderText() {
        return orderText;
    }

    public void setOrderText(String orderText) {
        this.orderText = orderText;
    }
}
