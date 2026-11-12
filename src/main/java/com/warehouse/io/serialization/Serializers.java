package com.warehouse.io.serialization;

import java.nio.file.Path;

public final class Serializers {

    private static final Serializer[] ALL = {
//            new JsonBatchSerializer(), //эти файлы надо прописать
//            new BinaryBatchSerializer()
    };

    private Serializers() { }

    public static Serializer forFile(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        for (Serializer s : ALL) {
            if (name.endsWith("." + s.format())) {
                return s;
            }
        }
        throw new IllegalArgumentException("Неизвестный формат файла (нужен .json или .bin): " + name);
    }

    public static Serializer[] all() {
        return ALL.clone();
    }
}
