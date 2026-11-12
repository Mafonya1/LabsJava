package com.warehouse.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class ArchivedBatch extends Batch implements Serializable {

    @JsonCreator
    public ArchivedBatch(@JsonProperty("article") int article, @JsonProperty("name") String name, @JsonProperty("quantity") int quantity, @JsonProperty("cell") int cell, @JsonProperty("arrivalDate") String arrivalDate) {
        super(article, name, quantity, cell, arrivalDate);
    }
}