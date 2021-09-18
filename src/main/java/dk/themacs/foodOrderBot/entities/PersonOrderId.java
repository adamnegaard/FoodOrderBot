package dk.themacs.foodOrderBot.entities;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PersonOrderId implements Serializable {
    private String userId;
    private long batchOrderId;

    public PersonOrderId() {
    }

    public PersonOrderId(String userId, long batchOrderId) {
        this.userId = userId;
        this.batchOrderId = batchOrderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getBatchOrderId() {
        return batchOrderId;
    }

    public void setBatchOrderId(long batchOrderId) {
        this.batchOrderId = batchOrderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonOrderId)) return false;
        PersonOrderId that = (PersonOrderId) o;
        return Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getBatchOrderId(), that.getBatchOrderId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getBatchOrderId());
    }

}
