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
                if (lineNo == 1) {
                    try {
                        checkHeader(line);
                        continue;
                    } catch (CsvLoadException e) {
                        errors.add(new LoadError(lineNo, line, e.getCode(), e.getMessage()));
                        break;
                    }
                }
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

    public void checkHeader(String line) {
        String text = line.replace("\uFEFF", "").trim();    // Excel может добавить в начало невидимый символ BOM
        if (!CsvFormat.HEADER.equals(text)) {
            throw new CsvLoadException(LoadError.ErrorCode.BAD_HEADER,
                    "Ожидался заголовок: " + CsvFormat.HEADER);
        }
    }

    /**
     * Разбирает одну строку с данными в партию.
     */
    public Batch parseLine(String line) {
        String[] p = CsvFormat.split(line);
        if (p.length != CsvFormat.COLUMNS) {
            throw new CsvLoadException(LoadError.ErrorCode.WRONG_FIELD_COUNT,
                    "Нужно " + CsvFormat.COLUMNS + " полей, есть " + p.length);
        }

        String type = p[0].trim();
        int article = parseInt(p[1], "article");
        String name = p[2].trim();
        int quantity = parseInt(p[3], "quantity");
        int cell = parseInt(p[4], "cell");
        String date = p[5].trim();

        if (CsvFormat.TYPE_ARCHIVED.equalsIgnoreCase(type)) {
            return new ArchivedBatch(article, name, quantity, cell, date);
        }
        if (CsvFormat.TYPE_IMPORTED.equalsIgnoreCase(type)) {
            return new ImportedBatch(article, name, quantity, cell, date, p[6].trim(), p[7].trim());
        }
        throw new CsvLoadException(LoadError.ErrorCode.UNKNOWN_TYPE,
                "Неизвестный тип: '" + type + "' (нужно imported или archived)");
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