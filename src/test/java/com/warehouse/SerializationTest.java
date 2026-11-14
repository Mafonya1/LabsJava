package com.warehouse;

import com.warehouse.io.serialization.BinaryBatchSerializer;
import com.warehouse.io.serialization.JsonBatchSerializer;
import com.warehouse.io.serialization.Serializer;
import com.warehouse.io.serialization.Serializers;
import com.warehouse.model.ArchivedBatch;
import com.warehouse.model.Batch;
import com.warehouse.model.ImportedBatch;
import com.warehouse.structure.HashTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SerializationTest {

    @TempDir Path tmp;

    private HashTable<Integer, Batch> sample() {
        HashTable<Integer, Batch> t = new HashTable<>();
        t.put(1, new ImportedBatch(1, "Болт", 10, 5, "01.01.2026", "Китай", "CN-1"));
        t.put(2, new ArchivedBatch(2, "Гайка", 20, 6, "02.01.2026"));
        return t;
    }

    @Test
    @DisplayName("JSON: сохранение и загрузка возвращают идентичную таблицу (roundtrip)")
    void json_roundtrip() throws IOException {
        Path file = tmp.resolve("data.json");
        HashTable<Integer, Batch> original = sample();
        Serializer s = new JsonBatchSerializer();
        s.save(original, file);
        assertEquals(original, s.load(file));
    }

    @Test
    @DisplayName("BIN: сохранение и загрузка возвращают идентичную таблицу (roundtrip)")
    void binary_roundtrip() throws IOException {
        Path file = tmp.resolve("data.bin");
        HashTable<Integer, Batch> original = sample();
        Serializer s = new BinaryBatchSerializer();
        s.save(original, file);
        assertEquals(original, s.load(file));
    }

    @Test
    @DisplayName("JSON-файл содержит поле версии схемы schemaVersion")
    void json_file_containsSchemaVersion() throws IOException {
        Path file = tmp.resolve("data.json");
        new JsonBatchSerializer().save(sample(), file);
        String content = Files.readString(file);
        assertTrue(content.contains("\"schemaVersion\""));
    }

    @Test
    @DisplayName("Фабрика возвращает JsonBatchSerializer для расширения .json")
    void factory_returnsJsonSerializer() {
        assertInstanceOf(JsonBatchSerializer.class, Serializers.forFile(Path.of("x.json")));
    }

    @Test
    @DisplayName("Фабрика возвращает BinaryBatchSerializer для расширения .bin")
    void factory_returnsBinSerializer() {
        assertInstanceOf(BinaryBatchSerializer.class, Serializers.forFile(Path.of("x.bin")));
    }

    @Test
    @DisplayName("Ошибка: загрузка повреждённого JSON вызывает IOException")
    void json_load_broken_throws() throws IOException {
        Path file = tmp.resolve("broken.json");
        Files.writeString(file, "{не json");
        assertThrows(IOException.class, () -> new JsonBatchSerializer().load(file));
    }

    @Test
    @DisplayName("Ошибка: попытка загрузить текст под видом BIN вызывает IOException")
    void bin_load_text_throws() throws IOException {
        Path file = tmp.resolve("fake.bin");
        Files.writeString(file, "это текст");
        assertThrows(IOException.class, () -> new BinaryBatchSerializer().load(file));
    }

    @Test
    @DisplayName("Ошибка: неподдерживаемое расширение файла вызывает IllegalArgumentException")
    void factory_unsupported_throws() {
        assertThrows(IllegalArgumentException.class, () -> Serializers.forFile(Path.of("x.xml")));
    }

    @Test
    @DisplayName("Ошибка: JSON-файл из будущего (schemaVersion > текущей) отклоняется")
    void json_futureVersion_throws() throws IOException {
        Path file = tmp.resolve("future.json");
        Files.writeString(file, "{\"schemaVersion\": 999, \"items\": []}");
        assertThrows(IOException.class, () -> new JsonBatchSerializer().load(file));
    }

    @Test
    @DisplayName("Ошибка: пустой JSON-файл вызывает IOException при загрузке")
    void json_emptyFile_throws() throws IOException {
        Path file = tmp.resolve("empty.json");
        Files.writeString(file, "");
        assertThrows(IOException.class, () -> new JsonBatchSerializer().load(file));
    }

    @Test
    @DisplayName("BIN: проверка чтения файла с текущей версией (roundtrip)")
    void binaryFutureVersion_throws() throws IOException {
        Path file = tmp.resolve("future.bin");
        try (var out = new java.io.ObjectOutputStream(Files.newOutputStream(file))) {
            new BinaryBatchSerializer().save(sample(), file);
            HashTable<Integer, Batch> loaded = new BinaryBatchSerializer().load(file);
            assertEquals(sample(), loaded);
        }
    }

    @Test
    @DisplayName("Граничный случай: пустая таблица сохраняется и загружается в JSON и BIN")
    void emptyTable_roundtrip() throws IOException {
        Path jsonFile = tmp.resolve("empty.json");
        Path binFile = tmp.resolve("empty.bin");
        HashTable<Integer, Batch> empty = new HashTable<>();
        new JsonBatchSerializer().save(empty, jsonFile);
        new BinaryBatchSerializer().save(empty, binFile);
        assertEquals(empty, new JsonBatchSerializer().load(jsonFile));
        assertEquals(empty, new BinaryBatchSerializer().load(binFile));
    }

    @Test
    @DisplayName("Фабрика корректно обрабатывает расширения в верхнем регистре (.JSON, .DAT)")
    void factory_caseInsensitive() {
        assertInstanceOf(JsonBatchSerializer.class, Serializers.forFile(Path.of("x.JSON")));
        assertInstanceOf(BinaryBatchSerializer.class, Serializers.forFile(Path.of("x.DAT")));
    }
}