package com.warehouse.service;

import com.warehouse.io.CsvLoader;
import com.warehouse.io.CsvSaver;
import com.warehouse.io.LoadResult;
import com.warehouse.model.Batch;
import com.warehouse.model.Editable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class BatchService {

    private final Map<Integer, Batch> store = new LinkedHashMap<>();
    private final CsvLoader loader = new CsvLoader();
    private final CsvSaver saver = new CsvSaver();

    public LoadResult load(Path file) {
        LoadResult result = loader.load(file);
        for (Batch b : result.loaded()) store.put(b.getArticle(), b);
        return result;
    }

    public void save(Path file) throws IOException {
        saver.save(new ArrayList<>(store.values()), file);
    }

    public void add(Batch b) {
        if (!(b instanceof Editable e)) {
            throw new IllegalArgumentException("Добавлять можно только редактируемые типы");
        }
        List<String> errors = e.validate();
        if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors));
        if (store.containsKey(b.getArticle())) {
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

    public List<Batch> findAll() { return new ArrayList<>(store.values()); }
}