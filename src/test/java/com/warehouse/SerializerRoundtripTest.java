package com.warehouse;

import com.warehouse.io.serialization.BinaryBatchSerializer;
import com.warehouse.io.serialization.JsonBatchSerializer;
import com.warehouse.io.serialization.Serializer;
import com.warehouse.io.serialization.Serializers;
import com.warehouse.model.ArchivedBatch;
import com.warehouse.model.Batch;
import com.warehouse.model.ImportedBatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SerializerRoundtripTest {

    private static Batch[] sample() {
        return new Batch[]{
                new ImportedBatch(101, "Болт", 15, 12, "24.09.2026", "Китай", "7318.15"),
                new ArchivedBatch(102, "Гайка", 3, 5, "20.09.2026"),
                new ImportedBatch(103, "Шайба", 0, 1, "01.10.2026", "Германия", "7318.22")
        };
    }

    /** load(save(x)) должен вернуть данные, равные x. */
    private static void assertRoundtrip(Serializer serializer, Path file) throws IOException {
        Batch[] original = sample();

        serializer.save(original, file);
        Batch[] loaded = serializer.load(file);

        assertEquals(original.length, loaded.length);
        for (int i = 0; i < original.length; i++) {
            assertSameBatch(original[i], loaded[i]);
        }
    }

    private static void assertSameBatch(Batch expected, Batch actual) {
        assertEquals(expected.getClass(), actual.getClass());
        assertEquals(expected.getArticle(), actual.getArticle());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getQuantity(), actual.getQuantity());
        assertEquals(expected.getCell(), actual.getCell());
        assertEquals(expected.getArrivalDate(), actual.getArrivalDate());
        if (expected instanceof ImportedBatch e) {
            ImportedBatch a = (ImportedBatch) actual;
            assertEquals(e.getCountry(), a.getCountry());
            assertEquals(e.getCustomsCode(), a.getCustomsCode());
        }
    }

    @Test
    void json_roundtrip_keeps_all_fields(@TempDir Path dir) throws IOException {
        assertRoundtrip(new JsonBatchSerializer(), dir.resolve("data.json"));
    }

    @Test
    void binary_roundtrip_keeps_all_fields(@TempDir Path dir) throws IOException {
        assertRoundtrip(new BinaryBatchSerializer(), dir.resolve("data.bin"));
    }

    @Test
    void json_fixture_file_is_loaded_correctly() throws Exception {
        // эталонный файл лежит в src/test/resources
        Path file = Path.of(getClass().getResource("/fixture-batches.json").toURI());

        Batch[] loaded = new JsonBatchSerializer().load(file);

        assertEquals(3, loaded.length);
        assertInstanceOf(ImportedBatch.class, loaded[0]);
        assertEquals(101, loaded[0].getArticle());
        assertInstanceOf(ArchivedBatch.class, loaded[1]);
        assertEquals("Гайка", loaded[1].getName());
        assertEquals("Германия", ((ImportedBatch) loaded[2]).getCountry());
    }

    @Test
    void empty_array_survives_roundtrip(@TempDir Path dir) throws IOException {
        Serializer json = new JsonBatchSerializer();
        Path file = dir.resolve("empty.json");

        json.save(new Batch[0], file);

        assertEquals(0, json.load(file).length);
    }

    @Test
    void broken_json_file_throws_io_exception(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("broken.json");
        Files.writeString(file, "{ это не json");

        assertThrows(IOException.class, () -> new JsonBatchSerializer().load(file));
    }

    @Test
    void serializer_is_chosen_by_file_extension() {
        assertEquals("json", Serializers.forFile(Path.of("data.json")).format());
        assertEquals("bin", Serializers.forFile(Path.of("data.BIN")).format());
        assertThrows(IllegalArgumentException.class, () -> Serializers.forFile(Path.of("data.txt")));
    }
}
