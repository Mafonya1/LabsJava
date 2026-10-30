package com.warehouse.service;

import com.warehouse.io.CsvLoader;
import com.warehouse.io.CsvSaver;
import com.warehouse.io.LoadResult;
import com.warehouse.model.Batch;
import com.warehouse.model.Editable;
import com.warehouse.structure.HashTable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public final class BatchService {

    // Хранилище - наша хеш-таблица (вместо LinkedHashMap из лабы 1). Ключ - артикул.
    private final HashTable<Integer, Batch> store = new HashTable<>();
    private final CsvLoader loader = new CsvLoader();
    private final CsvSaver saver = new CsvSaver();

    public LoadResult load(Path file) {
        LoadResult result = loader.load(file);
        addAll(result.loaded());
        return result;
    }

    /** Кладёт в таблицу пачку партий (из файла или сгенерированных). */
    public void addAll(List<Batch> batches) {
        for (Batch b : batches) {
            store.put(b.getArticle(), b);
        }
    }

    public void save(Path file) throws IOException {
        saver.save(findAll(), file);
    }

    public void add(Batch b) {
        if (!(b instanceof Editable e)) {
            throw new IllegalArgumentException("Добавлять можно только редактируемые типы");
        }
        List<String> errors = e.validate();
        if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors));
        // put заменил бы существующую партию (даже архивную), поэтому сначала проверяем
        if (store.get(b.getArticle()) != null) {
            throw new IllegalArgumentException("Партия с артикулом " + b.getArticle() + " уже существует");
        }
        store.put(b.getArticle(), b);
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

    /** Все партии списком. Обходим ячейки напрямую, чтобы не портить статистику проб. */
    public List<Batch> findAll() {
        List<Batch> result = new ArrayList<>();
        for (int i = 0; i < store.getCapacity(); i++) {
            if (store.getSlotState(i) == HashTable.SlotState.OCCUPIED) {
                result.add(store.getValueAt(i));
            }
        }
        return result;
    }

    /** Нужна визуализации, чтобы нарисовать таблицу. */
    public HashTable<Integer, Batch> getTable() { return store; }
}
