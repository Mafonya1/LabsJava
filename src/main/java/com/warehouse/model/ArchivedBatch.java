package com.warehouse.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;

public class ArchivedBatch extends Batch implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonCreator
    public ArchivedBatch(@JsonProperty("article") int article, @JsonProperty("name") String name,
                         @JsonProperty("quantity") int quantity, @JsonProperty("cell") int cell,
                         @JsonProperty("arrivalDate") String arrivalDate) {
        super(article, name, quantity, cell, arrivalDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArchivedBatch other)) return false;
        return super.equals(other);
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}