package com.warehouse.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ImportedBatch extends Batch implements Editable, Serializable {
    private final String country;
    private final String customsCode;

    @JsonCreator
    public ImportedBatch(@JsonProperty("article") int article, @JsonProperty("name") String name, @JsonProperty("quantity") int quantity, @JsonProperty("cell") int cell,
                         @JsonProperty("arrivalDate") String arrivalDate, @JsonProperty("country") String country, @JsonProperty("customsCode") String customsCode) {
        super(article, name, quantity, cell, arrivalDate);
        this.country = country == null ? "" : country;
        this.customsCode = customsCode == null ? "" : customsCode;
    }

    public String getCountry() { return country; }
    public String getCustomsCode() { return customsCode; }

    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        if (getArticle() <= 0) errors.add("Артикул должен быть > 0");
        if (getName().isBlank()) errors.add("Название не может быть пустым");
        if (getQuantity() < 0) errors.add("Количество не может быть отрицательным");
        if (getCell() <= 0) errors.add("Ячейка должна быть > 0");
        if (getArrivalDate().isBlank()) errors.add("Дата завоза обязательна");
        if (country.isBlank()) errors.add("Страна обязательна");
        if (customsCode.isBlank()) errors.add("Таможенный код обязателен");
        return errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImportedBatch other)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(country, other.country)
                && Objects.equals(customsCode, other.customsCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), country, customsCode);
    }
}