package com.warehouse.io.serialization;

import com.warehouse.model.Batch;

import java.io.IOException;
import java.nio.file.Path;


public interface Serializer {

    /** Короткое имя формата. Совпадает с расширением файла: "json", "bin". */
    String format();

    void save(Batch[] batches, Path file) throws IOException;

    Batch[] load(Path file) throws IOException;
}
