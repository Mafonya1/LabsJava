package com.warehouse.ui.task;

import com.warehouse.io.serialization.Serializer;
import com.warehouse.model.Batch;
import javafx.concurrent.Task;

import java.nio.file.Path;

/** Фоновое чтение файла. В структуру данные кладёт контроллер после успешного завершения. */
public class ImportTask extends Task<Batch[]> {

    private final Serializer serializer;
    private final Path file;

    public ImportTask(Serializer serializer, Path file) {
        this.serializer = serializer;
        this.file = file;
    }

    @Override
    protected Batch[] call() throws Exception {
        return serializer.load(file);
    }
}
