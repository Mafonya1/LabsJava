package com.warehouse.tools;

import com.warehouse.structure.HashTable;
import com.warehouse.structure.HashTableUnsafe;

import java.util.concurrent.CyclicBarrier;

public class ConcurrencyDemo {

    public static void main(String[] args) throws InterruptedException {
        int threads = 6;
        int items = 10000;
        int expected = threads * items;

        // 1. Тест БЕЗОПАСНОЙ таблицы
        HashTable<Integer, String> safeTable = new HashTable<>();
        runThreads(safeTable, null, threads, items);
        System.out.println("С СИНХРОНИЗАЦИЕЙ:");
        System.out.println("  Ожидалось: " + expected);
        System.out.println("  В таблице: " + safeTable.size());
        System.out.println("  Потеряно:  " + (expected - safeTable.size()));

        // 2. Тест НЕБЕЗОПАСНОЙ таблицы
        HashTableUnsafe<Integer, String> unsafeTable = new HashTableUnsafe<>();
        runThreads(null, unsafeTable, threads, items);
        System.out.println("БЕЗ СИНХРОНИЗАЦИИ:");
        System.out.println("  Ожидалось: " + expected);
        System.out.println("  В таблице: " + unsafeTable.size());
        System.out.println("  Потеряно:  " + (expected - unsafeTable.size()));
    }

    private static void runThreads(HashTable<Integer, String> safe, HashTableUnsafe<Integer, String> unsafe, int threads, int items) {
        Thread[] t = new Thread[threads];

        for (int i = 0; i < threads; i++) {
            final int id = i;
            t[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < items; j++) {
                        if (safe != null) {
                            safe.put(id * items + j, "val");
                        } else {
                            unsafe.put(id * items + j, "val");
                        }
                    }
                } catch (Exception e) { }
            });
            t[i].start();
        }

        for (int i = 0; i < threads; i++) {
            try { t[i].join(); } catch (InterruptedException e) { }
        }
    }
}