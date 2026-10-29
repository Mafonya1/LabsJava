package com.warehouse.io.serialization;

import com.warehouse.model.Batch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Бинарный формат средствами JDK: Batch реализует java.io.Serializable,
 * поэтому ObjectOutputStream сам превращает объекты в байты.
 * Записывается целый массив Batch[] одним вызовом writeObject.
 */
public final class BinaryBatchSerializer implements Serializer {

    @Override
    public String format() {
        return "bin";
    }

    @Override
    public void save(Batch[] batches, Path file) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(file)))) {
            out.writeObject(batches);
        }
    }

    @Override
    public Batch[] load(Path file) throws IOException {
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {
            Object data = in.readObject();
            if (data instanceof Batch[] batches) {
                return batches;
            }
            throw new IOException("В файле лежит не массив партий");
        } catch (ClassNotFoundException e) {
            throw new IOException("В файле неизвестный класс: " + e.getMessage(), e);
        }
    }
}
