package com.warehouse.ui.task;

import com.warehouse.model.Batch;
import com.warehouse.service.BatchGenerator;
import javafx.concurrent.Task;

/**
 * Фоновая генерация N партий. Как и LoadCsvTask, только создаёт объекты;
 * в хеш-таблицу их добавит контроллер в потоке JavaFX.
 */
public class GenerateTask extends Task<Batch[]> {

    private static final int PROGRESS_STEP = 500;

    private final int count;

    public GenerateTask(int count) {
        this.count = count;
    }

    @Override
    protected Batch[] call() {
        BatchGenerator generator = new BatchGenerator();
        Batch[] result = new Batch[count];
        for (int i = 0; i < count; i++) {
            result[i] = generator.create(i);
            if (i % PROGRESS_STEP == 0) {
                updateProgress(i, count);
            }
        }
        updateProgress(count, count);
        return result;
    }
}
