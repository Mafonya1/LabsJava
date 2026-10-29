package com.warehouse.service;

import com.warehouse.model.Batch;
import com.warehouse.model.ImportedBatch;

import java.util.Random;

public final class BatchGenerator {

    private static final String[] NAMES = {
            "Болт", "Гайка", "Шайба", "Кабель", "Подшипник", "Фильтр", "Датчик", "Реле"
    };
    private static final String[] COUNTRIES = {
            "Китай", "Германия", "Турция", "Индия", "Япония", "Польша"
    };

    private static final int ARTICLE_BASE = 100_000;
    private static final int ARTICLE_RANGE = 900_000_000;
    // Число, взаимно простое с ARTICLE_RANGE: тогда index -> (offset + index * STEP) % RANGE
    // даёт РАЗНЫЕ артикулы для разных index (повторов внутри одной генерации не будет),
    // но выглядят они случайными - в таблице появляются коллизии.
    private static final long ARTICLE_STEP = 2_654_435_761L;

    private static final int MAX_QUANTITY = 1000;
    private static final int MAX_CELL = 500;

    private final Random random;
    private final int offset;

    public BatchGenerator() {
        this(new Random());
    }

    /** Фиксированный seed - одинаковые данные при каждом запуске (удобно для тестов и замеров). */
    public BatchGenerator(long seed) {
        this(new Random(seed));
    }

    private BatchGenerator(Random random) {
        this.random = random;
        this.offset = random.nextInt(ARTICLE_RANGE);
    }

    public Batch create(int index) {
        int article = ARTICLE_BASE + (int) ((offset + index * ARTICLE_STEP) % ARTICLE_RANGE);
        String name = NAMES[random.nextInt(NAMES.length)] + "-" + (article % 1000);
        int quantity = random.nextInt(MAX_QUANTITY);
        int cell = 1 + random.nextInt(MAX_CELL);
        String date = "%02d.%02d.2026".formatted(1 + random.nextInt(28), 1 + random.nextInt(12));
        String country = COUNTRIES[random.nextInt(COUNTRIES.length)];
        String customsCode = "%04d.%02d".formatted(1000 + random.nextInt(9000), random.nextInt(100));
        return new ImportedBatch(article, name, quantity, cell, date, country, customsCode);
    }
}
