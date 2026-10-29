package com.warehouse.ui.task;

import com.warehouse.io.CsvLoader;
import com.warehouse.io.LoadResult;
import javafx.concurrent.Task;

import java.nio.file.Path;

/**
 * Фоновая загрузка CSV. call() выполняется в ОТДЕЛЬНОМ потоке, поэтому окно не зависает.
 * Файл только ПАРСИТСЯ (в таблицу ничего не кладётся) - в структуру данные добавит контроллер
 * в потоке JavaFX после завершения (setOnSucceeded). Так фоновый поток и интерфейс
 * никогда не обращаются к хеш-таблице одновременно.
 */
public class LoadCsvTask extends Task<LoadResult> {

    private final Path file;

    public LoadCsvTask(Path file) {
        this.file = file;
    }

    @Override
    protected LoadResult call() {
        // updateProgress(сделано, всего) двигает ProgressBar, привязанный к task.progressProperty()
        return new CsvLoader().load(file, (done, total) -> updateProgress(done, total));
    }
}
