package com.warehouse.io.serialization;

import com.warehouse.model.Batch;
import com.warehouse.structure.HashTable;

import java.nio.file.Path;

public final class Serializers {

    private Serializers() { }

    public static Serializer forFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".json")) {
            return new JsonBatchSerializer();
        } else if (fileName.endsWith(".bin") || fileName.endsWith(".dat")) {
            return new BinaryBatchSerializer();
        }
        throw new IllegalArgumentException("Неподдерживаемый формат файла: " + fileName);
    }
}
