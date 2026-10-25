package com.warehouse.model;

import java.io.Serializable;

public abstract class Batch implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int article;
    private final String name;
    private final int quantity;
    private final int cell;
    private final String arrivalDate;

    protected Batch(int article, String name, int quantity, int cell, String arrivalDate) {
        this.article = article;
        this.name = name == null ? "" : name;
        this.quantity = quantity;
        this.cell = cell;
        this.arrivalDate = arrivalDate == null ? "" : arrivalDate;
    }

    public int getArticle() { return article; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public int getCell() { return cell; }
    public String getArrivalDate() { return arrivalDate; }

    @Override
    public String toString() {
        return "Партия{артикул=%d, название='%s', количество=%d, ячейка=%d, дата='%s'}"
                .formatted(article, name, quantity, cell, arrivalDate);
    }
}