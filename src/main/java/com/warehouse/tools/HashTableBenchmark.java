package com.warehouse.tools;

import com.warehouse.model.ArchivedBatch;
import com.warehouse.model.Batch;
import com.warehouse.structure.HashTable;

import java.util.Random;

/**
 * Замеры времени для отчёта (таблица для среднего уровня).
 * Запуск: правая кнопка на файле -> Run 'HashTableBenchmark.main()'.
 * В консоль выводится готовая таблица: время в мс и среднее число проб на операцию.
 */
public class HashTableBenchmark {

    private static final int RANGE = 100000000;   // ключи берём из [0, RANGE)

    public static void main(String[] args) {
        int[] sizes = {10000, 100000};
        String[] names = {"Вставка", "Поиск (ключ есть)", "Поиск (ключа нет)", "Удаление"};

        // первый запуск "прогревает" Java, его результат не берём
        measure(10000, new double[4], new double[4]);

        double[][] ms = new double[sizes.length][4];
        double[][] probes = new double[sizes.length][4];
        for (int s = 0; s < sizes.length; s++) {
            measure(sizes[s], ms[s], probes[s]);
        }

        System.out.println("| Операция | " + sizes[0] + " записей | " + sizes[1] + " записей |");
        System.out.println("|---|---|---|");
        for (int op = 0; op < 4; op++) {
            System.out.println("| " + names[op]
                    + " | " + format(ms[0][op], probes[0][op])
                    + " | " + format(ms[1][op], probes[1][op]) + " |");
        }
    }

    private static String format(double ms, double probes) {
        return String.format("%.1f мс, проб на операцию: %.2f", ms, probes);
    }

    /** Меряет 4 операции на n записях. Результаты кладёт в массивы ms и probes. */
    private static void measure(int n, double[] ms, double[] probes) {
        Random random = new Random(42);
        int[] keys = new int[n];
        for (int i = 0; i < n; i++) {
            keys[i] = random.nextInt(RANGE);
        }
        Batch value = new ArchivedBatch(1, "тест", 1, 1, "01.01.2026");
        HashTable<Integer, Batch> table = new HashTable<>();
        long startProbes;
        long startOperations;
        long startTime;

        // 1. вставка
        startProbes = table.getTotalProbes();
        startOperations = table.getOperationCount();
        startTime = System.nanoTime();
        for (int i = 0; i < n; i++) {
            table.put(keys[i], value);
        }
        ms[0] = (System.nanoTime() - startTime) / 1000000.0;
        probes[0] = (double) (table.getTotalProbes() - startProbes) / (table.getOperationCount() - startOperations);

        // 2. поиск ключей, которые есть
        startProbes = table.getTotalProbes();
        startOperations = table.getOperationCount();
        startTime = System.nanoTime();
        for (int i = 0; i < n; i++) {
            table.get(keys[i]);
        }
        ms[1] = (System.nanoTime() - startTime) / 1000000.0;
        probes[1] = (double) (table.getTotalProbes() - startProbes) / (table.getOperationCount() - startOperations);

        // 3. поиск ключей, которых нет (они больше RANGE)
        startProbes = table.getTotalProbes();
        startOperations = table.getOperationCount();
        startTime = System.nanoTime();
        for (int i = 0; i < n; i++) {
            table.get(RANGE + i);
        }
        ms[2] = (System.nanoTime() - startTime) / 1000000.0;
        probes[2] = (double) (table.getTotalProbes() - startProbes) / (table.getOperationCount() - startOperations);

        // 4. удаление
        startProbes = table.getTotalProbes();
        startOperations = table.getOperationCount();
        startTime = System.nanoTime();
        for (int i = 0; i < n; i++) {
            table.remove(keys[i]);
        }
        ms[3] = (System.nanoTime() - startTime) / 1000000.0;
        probes[3] = (double) (table.getTotalProbes() - startProbes) / (table.getOperationCount() - startOperations);
    }
}
