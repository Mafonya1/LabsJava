package com.warehouse.io.serialization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.warehouse.model.Batch;
import com.warehouse.structure.HashTable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class JsonBatchSerializer implements Serializer{

    private final ObjectMapper mapper;

    public JsonBatchSerializer() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public String format() {
        return "json";
    }

    @Override
    public void save(HashTable<Integer, Batch> table, Path file) throws IOException {
        // Достаём все элементы из таблицы (например, через ваш метод values() или итератор)
        List<Batch> items = table.values();
        mapper.writeValue(file.toFile(), items);
    }

    @Override
    public HashTable<Integer, Batch> load(Path file) throws IOException {
        List<Batch> items = mapper.readValue(file.toFile(), new TypeReference<List<Batch>>() {});
        HashTable<Integer, Batch> table = new HashTable<>();
        for (Batch batch : items) {
            table.put(batch.getArticle(), batch); // заново раскладываем по хэш-таблице
        }
        return table;
    }
}
