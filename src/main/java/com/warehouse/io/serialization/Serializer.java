package com.warehouse.io.serialization;

import com.warehouse.model.Batch;
import com.warehouse.structure.HashTable;

import java.io.IOException;
import java.nio.file.Path;


public interface Serializer {

    /** Короткое имя формата. Совпадает с расширением файла: "json", "bin". */
    String format();

    void save(HashTable<Integer, Batch> table, Path file) throws IOException;

    HashTable<Integer, Batch> load(Path file) throws IOException;
}
