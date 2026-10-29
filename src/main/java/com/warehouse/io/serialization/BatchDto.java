package com.warehouse.io.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.warehouse.model.ArchivedBatch;
import com.warehouse.model.Batch;
import com.warehouse.model.ImportedBatch;

/**
 * DTO - плоская «карточка» партии для JSON. Jackson не знает, какой именно подкласс Batch
 * лежит в массиве, поэтому тип пишем явным полем type, а лишние поля архивной партии опускаем.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BatchDto(String type, int article, String name, int quantity, int cell,
                       String arrivalDate, String country, String customsCode) {

    public static final String TYPE_IMPORTED = "imported";
    public static final String TYPE_ARCHIVED = "archived";

    public static BatchDto from(Batch b) {
        if (b instanceof ImportedBatch ib) {
            return new BatchDto(TYPE_IMPORTED, b.getArticle(), b.getName(), b.getQuantity(), b.getCell(),
                    b.getArrivalDate(), ib.getCountry(), ib.getCustomsCode());
        }
        if (b instanceof ArchivedBatch) {
            return new BatchDto(TYPE_ARCHIVED, b.getArticle(), b.getName(), b.getQuantity(), b.getCell(),
                    b.getArrivalDate(), null, null);
        }
        throw new IllegalArgumentException("Неизвестный тип партии: " + b.getClass().getName());
    }

    public Batch toBatch() {
        if (TYPE_IMPORTED.equals(type)) {
            return new ImportedBatch(article, name, quantity, cell, arrivalDate, country, customsCode);
        }
        if (TYPE_ARCHIVED.equals(type)) {
            return new ArchivedBatch(article, name, quantity, cell, arrivalDate);
        }
        throw new IllegalArgumentException("Неизвестный тип партии в файле: " + type);
    }
}
