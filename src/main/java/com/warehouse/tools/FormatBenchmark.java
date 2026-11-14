package com.warehouse.tools;

import com.warehouse.io.serialization.BinaryBatchSerializer;
import com.warehouse.io.serialization.JsonBatchSerializer;
import com.warehouse.io.serialization.Serializer;
import com.warehouse.model.Batch;
import com.warehouse.model.ImportedBatch;
import com.warehouse.structure.HashTable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * Сравнение форматов JSON и BIN на 10 000 записей.
 * Выводит в консоль таблицу: размер файла, время сохранения и загрузки.
 * Запуск: правая кнопка -> Run 'FormatBenchmark.main()'.
 */
public class FormatBenchmark {

    private static final int N = 10_000;
    private static final int RUNS = 5;

    public static void main(String[] args) throws IOException {
        HashTable<Integer, Batch> table = buildTable(N);

        Path jsonFile = Path.of("benchmark-data.json");
        Path binFile = Path.of("benchmark-data.bin");

        Serializer json = new JsonBatchSerializer();
        Serializer bin = new BinaryBatchSerializer();

        long jsonSaveNano = timeSave(json, table, jsonFile);
        long binSaveNano = timeSave(bin, table, binFile);

        long jsonLoadNano = timeLoad(json, jsonFile);
        long binLoadNano = timeLoad(bin, binFile);

        long jsonSize = Files.size(jsonFile);
        long binSize = Files.size(binFile);

        System.out.println();
        System.out.println("Сравнение форматов на " + N + " записей (среднее по " + RUNS + " прогонам)");
        System.out.println("-------------------------------------------------------");
        System.out.printf("| %-15s | %-15s | %-15s |%n", "Метрика", "JSON", "BIN");
        System.out.println("|-----------------|-----------------|-----------------|");
        System.out.printf("| %-15s | %10d КБ | %10d КБ |%n",
                "Размер файла", jsonSize / 1024, binSize / 1024);
        System.out.printf("| %-15s | %10.1f мс | %10.1f мс |%n",
                "Сохранение", jsonSaveNano / 1_000_000.0, binSaveNano / 1_000_000.0);
        System.out.printf("| %-15s | %10.1f мс | %10.1f мс |%n",
                "Загрузка", jsonLoadNano / 1_000_000.0, binLoadNano / 1_000_000.0);
        System.out.println("-------------------------------------------------------");

        System.out.println("\nФрагмент JSON (первые 400 символов):");
        System.out.println(readFirstChars(jsonFile, 400));

        System.out.println("\nФрагмент BIN (первые 40 символов):");
        System.out.println(readFirstChars(binFile, 40));

        HashTable<Integer, Batch> jsonLoaded = json.load(jsonFile);
        HashTable<Integer, Batch> binLoaded = bin.load(binFile);
        System.out.println("JSON вернул корректно: " + table.equals(jsonLoaded));
        System.out.println("BIN  вернул корректно: " + table.equals(binLoaded));

        Files.deleteIfExists(jsonFile);
        Files.deleteIfExists(binFile);
    }

    private static HashTable<Integer, Batch> buildTable(int n) {
        Random rnd = new Random(42);   // seed, чтобы данные были одинаковые
        HashTable<Integer, Batch> t = new HashTable<>();
        for (int i = 0; i < n; i++) {
            t.put(i, new ImportedBatch(
                    i,
                    "Партия-" + i,
                    rnd.nextInt(1000),
                    1 + rnd.nextInt(500),
                    "01.01.2026",
                    "Китай",
                    "CN-" + i
            ));
        }
        return t;
    }

    private static long timeSave(Serializer serializer, HashTable<Integer, Batch> table,
                                 Path file) throws IOException {
        long total = 0;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            serializer.save(table, file);
            total += System.nanoTime() - start;
        }
        return total / RUNS;
    }

    private static long timeLoad(Serializer serializer, Path file) throws IOException {
        long total = 0;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            serializer.load(file);
            total += System.nanoTime() - start;
        }
        return total / RUNS;
    }

    private static String readFirstChars(Path file, int count) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        int len = Math.min(count, bytes.length);
        return new String(bytes, 0, len, java.nio.charset.StandardCharsets.UTF_8);
    }
}