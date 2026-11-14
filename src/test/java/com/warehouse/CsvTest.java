package com.warehouse;

import com.warehouse.io.CsvLoadException;
import com.warehouse.io.CsvLoader;
import com.warehouse.io.LoadError;
import com.warehouse.io.LoadResult;
import com.warehouse.model.ArchivedBatch;
import com.warehouse.model.ImportedBatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CsvTest {

    @TempDir Path tmp;

    @Test
    @DisplayName("Парсинг валидной строки импортной партии")
    void parseLine_imported() {
        CsvLoader loader = new CsvLoader();
        String line = "imported;1;Болт;10;5;01.01.2026;Китай;CN-1";
        var batch = loader.parseLine(line);
        assertInstanceOf(ImportedBatch.class, batch);
        assertEquals(1, batch.getArticle());
    }

    @Test
    @DisplayName("Парсинг валидной строки архивной партии")
    void parseLine_archived() {
        CsvLoader loader = new CsvLoader();
        String line = "archived;2;Гайка;20;6;02.01.2026;;";
        var batch = loader.parseLine(line);
        assertInstanceOf(ArchivedBatch.class, batch);
        assertEquals(2, batch.getArticle());
    }

    @Test
    @DisplayName("Парсинг поля в кавычках с точкой с запятой внутри")
    void parseLine_withQuotes() {
        CsvLoader loader = new CsvLoader();
        String line = "imported;1;\"Болт; большой\";10;5;01.01.2026;Китай;CN-1";
        var batch = loader.parseLine(line);
        assertEquals("Болт; большой", batch.getName());
    }

    @Test
    @DisplayName("Ошибка: нечисловое значение в числовом поле вызывает CsvLoadException (BAD_NUMBER)")
    void parseLine_brokenNumber_throwsException() {
        CsvLoader loader = new CsvLoader();
        String line = "imported;abc;Болт;10;5;01.01.2026;Китай;CN-1";
        CsvLoadException ex = assertThrows(CsvLoadException.class, () -> loader.parseLine(line));
        assertEquals(LoadError.ErrorCode.BAD_NUMBER, ex.getCode());
    }

    @Test
    @DisplayName("Загрузка файла пропускает битые строки и собирает ошибки")
    void load_skipsBrokenLines_andCollectsErrors() throws IOException {
        Path file = tmp.resolve("data.csv");
        Files.writeString(file, """
                type;article;name;quantity;cell;arrivalDate;country;customsCode
                imported;1;Болт;10;5;01.01.2026;Китай;CN-1
                bad_line;without;enough;fields
                imported;2;Гайка;20;6;02.01.2026;Китай;CN-2
                """);
        CsvLoader loader = new CsvLoader();
        LoadResult result = loader.load(file);
        assertEquals(2, result.loaded().size());
        assertEquals(1, result.errors().size());
        assertEquals(LoadError.ErrorCode.WRONG_FIELD_COUNT, result.errors().get(0).code());
    }
}