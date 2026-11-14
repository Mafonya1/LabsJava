package com.warehouse.ui.task;

import com.warehouse.io.serialization.Serializer;
import com.warehouse.model.Batch;
import com.warehouse.structure.HashTable;
import javafx.concurrent.Task;

import java.nio.file.Path;

public class ExportTask extends Task<Void>{
    private final Serializer serializer;
    private final HashTable<Integer, Batch> snapshot;
    private final Path file;

    public ExportTask(Serializer serializer, HashTable<Integer, Batch> snapshot, Path file) {
        this.serializer = serializer;
        this.snapshot = snapshot;
        this.file = file;
    }

    @Override
    protected Void call() throws Exception {
        if (isCancelled()) {
            return null;                 // пользователь нажал "Отмена"
        }
        serializer.save(snapshot, file);
        return null;
    }
}
