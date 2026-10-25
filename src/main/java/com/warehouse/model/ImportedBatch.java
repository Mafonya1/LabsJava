package com.warehouse.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ImportedBatch extends Batch implements Editable, Serializable {
    private static final long serialVersionUID = 1L;

    private final String country;
    private final String customsCode;

    public ImportedBatch(int article, String name, int quantity, int cell,
                         String arrivalDate, String country, String customsCode) {
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
}