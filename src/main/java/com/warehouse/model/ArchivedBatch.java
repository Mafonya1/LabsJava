package com.warehouse.model;

import java.io.Serializable;

public class ArchivedBatch extends Batch implements Serializable {

    public ArchivedBatch(int article, String name, int quantity, int cell, String arrivalDate) {
        super(article, name, quantity, cell, arrivalDate);
    }
}