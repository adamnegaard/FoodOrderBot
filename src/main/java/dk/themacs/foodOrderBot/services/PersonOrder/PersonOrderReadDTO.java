package dk.themacs.foodOrderBot.services.PersonOrder;

import dk.themacs.foodOrderBot.entities.PersonOrder;

public class PersonOrderReadDTO {
    private long id;
    private String userId;
    private String orderTs;
    private Long batchOrderId;
    private String orderText;

    public PersonOrderReadDTO() {}

    public PersonOrderReadDTO(PersonOrder personOrder) {
        this.id = personOrder.getId();
        this.userId = personOrder.getUserId();
        this.orderTs = personOrder.getOrderTs();
        this.batchOrderId = personOrder.getBatchOrder().getId();
        this.orderText = personOrder.getOrderText();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public Long getBatchOrderId() {
        return batchOrderId;
    }

    public void setBatchOrderId(Long batchOrderId) {
        this.batchOrderId = batchOrderId;
    }

    public String getOrderText() {
        return orderText;
    }

    public void setOrderText(String orderText) {
        this.orderText = orderText;
    }
}
