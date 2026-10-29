package com.warehouse.service;

import com.warehouse.io.CsvLoader;
import com.warehouse.io.CsvSaver;
import com.warehouse.io.LoadResult;
import com.warehouse.model.Batch;
import com.warehouse.model.Editable;
import com.warehouse.structure.HashTable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public final class BatchService {

    // Основное хранилище - собственная хеш-таблица (ключ - артикул).
    // LinkedHashMap из лаб. 1 убран: java.util-коллекции как хранилище запрещены.
    private final HashTable<Integer, Batch> store = new HashTable<>();
    private final CsvLoader loader = new CsvLoader();
    private final CsvSaver saver = new CsvSaver();

    /** Синхронная загрузка (используется при старте и в тестах). */
    public LoadResult load(Path file) {
        LoadResult result = loader.load(file);
        addLoaded(result.loaded());
        return result;
    }

    /** Кладёт уже прочитанные партии в таблицу (при совпадении артикула запись заменяется). */
    public void addLoaded(List<Batch> loaded) {
        for (Batch b : loaded) {
            store.put(b.getArticle(), b);
        }
    }

    public void save(Path file) throws IOException {
        // Список здесь - временный, только чтобы отдать данные CsvSaver. Хранилищем он не является.
        saver.save(Arrays.asList(findAll()), file);
    }

    public void add(Batch b) {
        if (!(b instanceof Editable e)) {
            throw new IllegalArgumentException("Добавлять можно только редактируемые типы");
        }
        List<String> errors = e.validate();
        if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors));
        // putIfAbsent: один проход по таблице, существующую партию (в т.ч. архивную) не затрёт
        if (!store.putIfAbsent(b.getArticle(), b)) {
            throw new IllegalArgumentException("Партия с артикулом " + b.getArticle() + " уже существует");
        }
    }

    public void update(Batch b) {
        Batch existing = store.get(b.getArticle());
        if (existing == null) throw new IllegalArgumentException("Партия не найдена");
        if (!(existing instanceof Editable)) throw new IllegalStateException("Партия read-only");
        if (!(b instanceof Editable e)) throw new IllegalArgumentException("Должен быть Editable");
        List<String> errors = e.validate();
        if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors));
        store.put(b.getArticle(), b);
    }

    public void delete(int article) { store.remove(article); }

    public Batch find(int article) { return store.get(article); }

    /**
     * Все партии массивом (в порядке ячеек таблицы).
     * Обходим ячейки напрямую, а не через get(), чтобы не портить статистику проб последней операции.
     */
    public Batch[] findAll() {
        Batch[] result = new Batch[store.size()];
        int n = 0;
        for (int i = 0; i < store.getCapacity(); i++) {
            if (store.getSlotState(i) == HashTable.SlotState.OCCUPIED) {
                result[n++] = store.getValueAt(i);
            }
        }
        return result;
    }

    /** Добавляет пачку (например, сгенерированную). Партии с уже занятым артикулом пропускаются. @return сколько добавлено. */
    public int addAll(Batch[] batches) {
        int added = 0;
        for (Batch b : batches) {
            if (b != null && store.putIfAbsent(b.getArticle(), b)) added++;
        }
        return added;
    }

    public void clear() { store.clear(); }

    public int size() { return store.size(); }

    /** Нужна визуализации, чтобы читать внутреннее устройство таблицы. */
    public HashTable<Integer, Batch> getTable() { return store; }
}
