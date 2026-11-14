package com.warehouse.io;

import com.warehouse.model.ArchivedBatch;
import com.warehouse.model.Batch;
import com.warehouse.model.ImportedBatch;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CsvSaver {

    public void save(List<Batch> batches, Path file) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write(CsvFormat.HEADER);                 // первая строка - заголовок
            w.newLine();
            for (Batch b : batches) {
                w.write(toLine(b));
                w.newLine();
            }
        }
    }

    private String toLine(Batch b) {
        String type;
        String country = "";
        String customsCode = "";

        if (b instanceof ImportedBatch ib) {
            type = CsvFormat.TYPE_IMPORTED;
            country = ib.getCountry();
            customsCode = ib.getCustomsCode();
        } else if (b instanceof ArchivedBatch) {
            type = CsvFormat.TYPE_ARCHIVED;
        } else {
            throw new IllegalArgumentException("Неизвестный тип: " + b.getClass());
        }

        return String.join(String.valueOf(CsvFormat.DELIMITER),
                type,
                String.valueOf(b.getArticle()),
                CsvFormat.escape(b.getName()),
                String.valueOf(b.getQuantity()),
                String.valueOf(b.getCell()),
                CsvFormat.escape(b.getArrivalDate()),
                CsvFormat.escape(country),
                CsvFormat.escape(customsCode));
    }
}