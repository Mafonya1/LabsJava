package com.warehouse.ui.task;

import com.warehouse.model.Batch;
import com.warehouse.model.ImportedBatch;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Генерация нужного числа партий в фоновом потоке. Создаёт только импортные партии. */
public class GenerateTask extends Task<List<Batch>> {

    private static final String[] NAMES = {"Болт", "Гайка", "Шайба", "Кабель", "Подшипник", "Фильтр"};
    private static final String[] COUNTRIES = {"Китай", "Германия", "Турция", "Индия", "Япония"};

    private final int count;

    public GenerateTask(int count) {
        this.count = count;
    }

    @Override
    protected List<Batch> call() {
        Random random = new Random();
        List<Batch> result = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            if (isCancelled()) {
                return null;                 // пользователь нажал "Отмена"
            }
            int article = 100000 + random.nextInt(100000000);
            String name = NAMES[random.nextInt(NAMES.length)] + "-" + (article % 1000);
            int quantity = random.nextInt(1000);
            int cell = 1 + random.nextInt(500);
            String date = String.format("%02d.%02d.2026", 1 + random.nextInt(28), 1 + random.nextInt(12));
            String country = COUNTRIES[random.nextInt(COUNTRIES.length)];
            String customsCode = String.format("%04d.%02d", 1000 + random.nextInt(9000), random.nextInt(100));

            result.add(new ImportedBatch(article, name, quantity, cell, date, country, customsCode));
            updateProgress(i + 1, count);
        }
        return result;
    }
}
