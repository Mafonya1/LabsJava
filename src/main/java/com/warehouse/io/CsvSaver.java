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
            for (Batch b : batches) {
                w.write(toLine(b));
                w.newLine();
            }
        }
    }

    private String toLine(Batch b) {
        if (b instanceof ArchivedBatch) {
            return "Archive;%d;%s;%d;%d;%s".formatted(
                    b.getArticle(), b.getName(), b.getQuantity(), b.getCell(), b.getArrivalDate());
        }
        if (b instanceof ImportedBatch ib) {
            return "%d;%s;%d;%d;%s;%s;%s".formatted(
                    b.getArticle(), b.getName(), b.getQuantity(), b.getCell(),
                    b.getArrivalDate(), ib.getCountry(), ib.getCustomsCode());
        }
        throw new IllegalArgumentException("Неизвестный тип: " + b.getClass());
    }
}