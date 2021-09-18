package dk.themacs.foodOrderBot.entities;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "person_order")
public class PersonOrder {

    @Id
    @SequenceGenerator(
            name = "person_order_sequence",
            sequenceName = "batch_order_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "person_order_sequence"
    )
    private long id;

    @Column(name = "user_id")
    private String userId;

    @ManyToOne(fetch = FetchType.EAGER)
    private BatchOrder batchOrder;

    @Column(name = "order_text")
    private String orderText;

    public PersonOrder() {
    }

    public PersonOrder(String userId, BatchOrder batchOrder, String orderText) {
        this.userId = userId;
        this.batchOrder = batchOrder;
        this.orderText = orderText;
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

    public BatchOrder getBatchOrder() {
        return batchOrder;
    }

    public void setBatchOrder(BatchOrder batchOrder) {
        this.batchOrder = batchOrder;
    }

    public String getOrderText() {
        return orderText;
    }

    public void setOrderText(String orderText) {
        this.orderText = orderText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonOrder)) return false;
        PersonOrder that = (PersonOrder) o;
        return Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getBatchOrder(), that.getBatchOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getBatchOrder());
    }
}


