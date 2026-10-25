package com.warehouse.io;

import com.warehouse.model.ArchivedBatch;
import com.warehouse.model.Batch;
import com.warehouse.model.ImportedBatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CsvLoader {

    public LoadResult load(Path file) {
        List<Batch> loaded = new ArrayList<>();
        List<LoadError> errors = new ArrayList<>();

        if (!Files.exists(file)) {
            errors.add(new LoadError(-1, "", LoadError.ErrorCode.UNKNOWN_TYPE,
                    "Файл не найден: " + file));
            return new LoadResult(loaded, errors);
        }

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) continue;
                try {
                    loaded.add(parseLine(line));
                } catch (CsvLoadException e) {
                    errors.add(new LoadError(lineNo, line, e.getCode(), e.getMessage()));
                }
            }
        } catch (IOException e) {
            errors.add(new LoadError(-1, "", LoadError.ErrorCode.UNKNOWN_TYPE,
                    "Ошибка чтения: " + e.getMessage()));
        }
        return new LoadResult(loaded, errors);
    }

    private Batch parseLine(String line) {
        String[] p = line.split(";", -1);
        if (p.length == 0) {
            throw new CsvLoadException(LoadError.ErrorCode.EMPTY_LINE, "Пустая строка");
        }

        String first = p[0].trim();

        if ("Archive".equalsIgnoreCase(first)) {
            if (p.length != 6) {
                throw new CsvLoadException(LoadError.ErrorCode.WRONG_FIELD_COUNT,
                        "Archive: нужно 6 полей, есть " + p.length);
            }
            int article = parseInt(p[1], "Archive.article");
            String name = p[2].trim();
            int qty   = parseInt(p[3], "Archive.quantity");
            int cell  = parseInt(p[4], "Archive.cell");
            String date = p[5].trim();
            return new ArchivedBatch(article, name, qty, cell, date);
        }

        if (p.length != 7) {
            throw new CsvLoadException(LoadError.ErrorCode.WRONG_FIELD_COUNT,
                    "Imported: нужно 7 полей, есть " + p.length);
        }
        int article = parseInt(first, "Imported.article");
        String name = p[1].trim();
        int qty     = parseInt(p[2], "Imported.quantity");
        int cell    = parseInt(p[3], "Imported.cell");
        String date = p[4].trim();
        String country = p[5].trim();
        String code    = p[6].trim();
        return new ImportedBatch(article, name, qty, cell, date, country, code);
    }

    private int parseInt(String raw, String fieldName) {
        String s = raw.trim();
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new CsvLoadException(LoadError.ErrorCode.BAD_NUMBER,
                    fieldName + ": не число — '" + s + "'");
        }
    }
}