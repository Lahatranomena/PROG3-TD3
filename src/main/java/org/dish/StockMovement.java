package org.dish;

import java.time.Instant;
import java.util.Objects;

public class StockMovement {
    private int id;
    private StockValue value;
    private MovementTypeEnum type;
    private Instant creation_datetime;

    public StockMovement(int id, StockValue value, MovementTypeEnum type, Instant creation_datetime) {
        this.id = id;
        this.value = value;
        this.type = type;
        this.creation_datetime = creation_datetime;
    }

    public StockMovement() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public StockValue getValue() {
        return value;
    }

    public void setValue(StockValue value) {
        this.value = value;
    }

    public MovementTypeEnum getType() {
        return type;
    }

    public void setType(MovementTypeEnum type) {
        this.type = type;
    }

    public Instant getCreationDatetime() {
        return creation_datetime;
    }

    public void setCreationDatetime(Instant creation_datetime) {
        this.creation_datetime = creation_datetime;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StockMovement that)) return false;
        return id == that.id && Objects.equals(value, that.value) && type == that.type && Objects.equals(creation_datetime, that.creation_datetime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value, type, creation_datetime);
    }

    @Override
    public String toString() {
        return "StockMovement{" +
                "id=" + id +
                ", value=" + value +
                ", type=" + type +
                ", creation_datetime=" + creation_datetime +
                '}';
    }
}
