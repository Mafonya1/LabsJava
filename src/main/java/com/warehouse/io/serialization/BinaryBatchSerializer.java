package com.warehouse.io.serialization;

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
            out.writeObject(table);
        }
    }

    @Override
    @SuppressWarnings("unckecked")
    public HashTable<Integer, Batch> load(Path file) throws IOException{
        try(ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {
            return (HashTable<Integer, Batch>) in.readObject();
        }
        catch (ClassNotFoundException e){
            throw new IOException("Не найден класс при демериализации", e);
        }
    }


}
