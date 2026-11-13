package com.warehouse.io.serialization;

import com.warehouse.io.serialization.envelopes.BinaryEnvelope;
import com.warehouse.model.Batch;
import com.warehouse.structure.HashTable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BinaryBatchSerializer implements Serializer {
    @Override
    public String format(){
        return "bin";
    }

    @Override
    public void save(HashTable<Integer, Batch> table, Path file) throws IOException{
        try(ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(file)))){
            BinaryEnvelope envelope = new BinaryEnvelope(BinaryEnvelope.CURRENT_VERSION, table);
            out.writeObject(envelope);
        }
    }

    @Override
    @SuppressWarnings("unckecked")
    public HashTable<Integer, Batch> load(Path file) throws IOException{
        try(ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {
            Object obj = in.readObject();
            if (!(obj instanceof BinaryEnvelope envelope)) {
                throw new IOException("Файл не является корректным BIN-файлом склада (ожидался BinaryEnvelope).");
            }
            if (envelope.getSchemaVersion() > BinaryEnvelope.CURRENT_VERSION) {
                throw new IOException("Файл создан более новой версией (v" + envelope.getSchemaVersion()
                        + "), приложение поддерживает до v" + BinaryEnvelope.CURRENT_VERSION);
            }
            return envelope.getTable();
        }
        catch (ClassNotFoundException e){
            throw new IOException("Не найден класс при демериализации", e);
        } catch (InvalidClassException e) {
            throw new IOException("Версия класса не совпадает (serialVersionUID). "
                    + "Файл создан другой версией приложения.", e);
        } catch (StreamCorruptedException e) {
            throw new IOException("Файл повреждён или не является BIN-файлом.", e);
        }
    }


}
