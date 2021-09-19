package dk.themacs.foodOrderBot.entities;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "batch_order")
public class BatchOrder {

    @Id
    @SequenceGenerator(
            name = "batch_order_sequence",
            sequenceName = "batch_order_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "batch_order_sequence"
    )
    private long id;
    @Column(name = "started_ts")
    private String startedTs;
    @Column(name = "is_ordered")
    private boolean isOrdered;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "batchOrder", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private Set<PersonOrder> personOrders = new HashSet<>();

    public BatchOrder() {
    }

    public BatchOrder(String startedTs) {
        this.startedTs = startedTs;
        isOrdered = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStartedTs() {
        return startedTs;
    }

    public void setStartedTs(String startedTs) {
        this.startedTs = startedTs;
    }

    public boolean isOrdered() {
        return isOrdered;
    }

    public void setOrdered(boolean ordered) {
        isOrdered = ordered;
    }

    public Set<PersonOrder> getPersonOrders() {
        return personOrders;
    }

    public void setPersonOrders(Set<PersonOrder> personOrders) {
        this.personOrders = personOrders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BatchOrder)) return false;
        BatchOrder that = (BatchOrder) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getStartedTs(), that.getStartedTs());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getStartedTs());
    }
}
