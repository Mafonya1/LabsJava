package com.warehouse.ui.task;

import com.warehouse.io.CsvLoadException;
import com.warehouse.io.CsvLoader;
import com.warehouse.io.LoadError;
import com.warehouse.io.LoadResult;
import com.warehouse.model.Batch;
import javafx.concurrent.Task;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Загрузка CSV в фоновом потоке, чтобы окно не зависало.
 * Метод call() выполняется в отдельном потоке. Он только читает и разбирает файл,
 * а в таблицу данные кладёт контроллер, когда задача закончится.
 */
public class LoadTask extends Task<LoadResult> {

    private final Path file;

    public LoadTask(Path file) {
        this.file = file;
    }

    @Override
    protected LoadResult call() throws Exception {
        CsvLoader loader = new CsvLoader();
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        List<Batch> loaded = new ArrayList<>();
        List<LoadError> errors = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            if (isCancelled()) {
                return null;                 // пользователь нажал "Отмена"
            }
            String line = lines.get(i);
            if (!line.isBlank()) {
                try {
                    loaded.add(loader.parseLine(line));
                } catch (CsvLoadException e) {
                    errors.add(new LoadError(i + 1, line, e.getCode(), e.getMessage()));
                }
            }
            updateProgress(i + 1, lines.size());   // двигает полоску прогресса
        }
        return new LoadResult(loaded, errors);
    }
}
