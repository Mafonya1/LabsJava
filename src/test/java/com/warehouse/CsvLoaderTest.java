package com.warehouse;

import com.warehouse.io.CsvLoader;
import com.warehouse.io.LoadResult;
import com.warehouse.model.ArchivedBatch;
import com.warehouse.model.ImportedBatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CsvLoaderTest {

    @TempDir Path tmp;

    @Test
    void parses_imported_line() throws Exception {
        Path f = tmp.resolve("a.csv");
        Files.writeString(f, "102;Партия;15;12;24.09.2026;Китай;CN-1\n");
        LoadResult r = new CsvLoader().load(f);
        assertEquals(1, r.loaded().size());
        assertInstanceOf(ImportedBatch.class, r.loaded().get(0));
        assertTrue(r.errors().isEmpty());
    }

    @Test
    void parses_archived_line() throws Exception {
        Path f = tmp.resolve("a.csv");
        Files.writeString(f, "Archive;101;Архив;15;5;20.09.2026\n");
        LoadResult r = new CsvLoader().load(f);
        assertInstanceOf(ArchivedBatch.class, r.loaded().get(0));
    }

    @Test
    void skips_broken_line_and_reports_error() throws Exception {
        Path f = tmp.resolve("a.csv");
        Files.writeString(f, "битая;строка\n");
        LoadResult r = new CsvLoader().load(f);
        assertEquals(0, r.loaded().size());
        assertEquals(1, r.errors().size());
    }
}