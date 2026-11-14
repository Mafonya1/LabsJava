package com.warehouse.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.*;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ImportedBatch.class, name = "imported"),
        @JsonSubTypes.Type(value = ArchivedBatch.class, name = "archived")
})

public abstract class Batch implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    public static final int CURRENT_VERSION = 2;

    private final int article;
    private final String name;
    private final int quantity;
    private final int cell;
    private final String arrivalDate;
    private final String changeDate;

    // Основной конструктор (v2)
    protected Batch(int article, String name, int quantity, int cell, String arrivalDate, String changeDate) {
        this.article = article;
        this.name = name == null ? "" : name;
        this.quantity = quantity;
        this.cell = cell;
        this.arrivalDate = arrivalDate == null ? "" : arrivalDate;
        this.changeDate = (changeDate == null || changeDate.isBlank()) ? arrivalDate : changeDate;
    }

    // Конструктор для миграции со старой версии (v1 -> v2)
    protected Batch(int article, String name, int quantity, int cell, String arrivalDate) {
        this(article, name, quantity, cell, arrivalDate, arrivalDate);
    }

    public int getArticle() {
        return article;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getCell() {
        return cell;
    }

    public String getArrivalDate() {
        return arrivalDate;
    }

    public String getChangeDate() {
        return changeDate;
    }

    @Override
    public String toString() {
        return "Партия{артикул=%d, название='%s', количество=%d, ячейка=%d, дата='%s', изменено='%s'}"
                .formatted(article, name, quantity, cell, arrivalDate, changeDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Batch other)) return false;
        return article == other.article
                && quantity == other.quantity
                && cell == other.cell
                && Objects.equals(name, other.name)
                && Objects.equals(arrivalDate, other.arrivalDate)
                && Objects.equals(changeDate, other.changeDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(article, name, quantity, cell, arrivalDate, changeDate);
    }
}