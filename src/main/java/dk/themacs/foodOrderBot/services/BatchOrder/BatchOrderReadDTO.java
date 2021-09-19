package dk.themacs.foodOrderBot.services.BatchOrder;

import dk.themacs.foodOrderBot.entities.BatchOrder;
import dk.themacs.foodOrderBot.entities.PersonOrder;
import dk.themacs.foodOrderBot.services.PersonOrder.PersonOrderReadDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BatchOrderReadDTO {
    private long id;
    private String startedTs;
    private boolean isOrdered;
    private Set<PersonOrderReadDTO> personOrders;

    public BatchOrderReadDTO() {}

    public BatchOrderReadDTO(BatchOrder batchOrder) {
        this.id = batchOrder.getId();
        this.startedTs = batchOrder.getStartedTs();
        this.isOrdered = batchOrder.isOrdered();
        this.personOrders = StreamSupport.stream(batchOrder.getPersonOrders().spliterator(), false)
                .map(personOrder -> new PersonOrderReadDTO(personOrder))
                .collect(Collectors.toCollection(LinkedHashSet::new));
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

    public Set<PersonOrderReadDTO> getPersonOrders() {
        return personOrders;
    }

    public void setPersonOrders(Set<PersonOrderReadDTO> personOrders) {
        this.personOrders = personOrders;
    }
}
