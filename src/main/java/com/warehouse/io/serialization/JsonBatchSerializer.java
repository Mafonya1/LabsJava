package com.warehouse.io.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.warehouse.io.serialization.envelopes.JsonEnvelope;
import com.warehouse.model.Batch;
import com.warehouse.structure.HashTable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class JsonBatchSerializer implements Serializer{

    private final ObjectMapper mapper;

    public JsonBatchSerializer() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public String format() {
        return "json";
    }

    @Override
    public void save(HashTable<Integer, Batch> table, Path file) throws IOException {
        List<Batch> items = table.values();
        JsonEnvelope envelope = new JsonEnvelope(JsonEnvelope.CURRENT_VERSION, items);
        mapper.writerFor(JsonEnvelope.class)
                .writeValue(file.toFile(), envelope);
    }


    @Override
    public HashTable<Integer, Batch> load(Path file) throws IOException {
        JsonEnvelope envelope;
        try {
            envelope = mapper.readValue(file.toFile(), JsonEnvelope.class);
        } catch (IOException e) {
            throw new IOException("Файл повреждён или не является JSON: " + e.getMessage(), e);
        }
        if (envelope.schemaVersion() > JsonEnvelope.CURRENT_VERSION) {
            throw new IOException("Файл создан более новой версией (v" + envelope.schemaVersion()
                    + "), приложение поддерживает до v" + JsonEnvelope.CURRENT_VERSION);
        }

        HashTable<Integer, Batch> table = new HashTable<>();
        for (Batch batch : envelope.items()) {
            table.put(batch.getArticle(), batch); // заново раскладываем по хэш-таблице
        }
        return table;
    }
}
