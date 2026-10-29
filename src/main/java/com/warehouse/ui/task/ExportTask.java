package com.warehouse.ui.task;

import com.warehouse.io.serialization.Serializer;
import com.warehouse.model.Batch;
import javafx.concurrent.Task;

import java.nio.file.Path;

/**
 * Фоновый экспорт. Получает СНИМОК данных (массив, снятый в потоке JavaFX),
 * поэтому не трогает хеш-таблицу: партии неизменяемые, гонки данных нет.
 */
public class ExportTask extends Task<Void> {

    private final Serializer serializer;
    private final Batch[] snapshot;
    private final Path file;

    public ExportTask(Serializer serializer, Batch[] snapshot, Path file) {
        this.serializer = serializer;
        this.snapshot = snapshot;
        this.file = file;
    }

    @Override
    protected Void call() throws Exception {
        serializer.save(snapshot, file);
        return null;
    }
}
