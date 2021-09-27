package dk.themacs.foodOrderBot.services.PersonOrder;

import dk.themacs.foodOrderBot.entities.BatchOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PersonOrderCreateDTO {
    private String userId;
    private String orderTs;
    private String batchOrderTs;
    private String orderText;

    public PersonOrderCreateDTO() {}

    public PersonOrderCreateDTO(String userId, String orderTs, String batchOrderTs, String orderText) {
        this.userId = userId;
        this.orderTs = orderTs;
        this.batchOrderTs = batchOrderTs;
        this.orderText = orderText;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrderTs() {
        return orderTs;
    }

    public void setOrderTs(String orderTs) {
        this.orderTs = orderTs;
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
