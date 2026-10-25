package com.warehouse.io;

import com.warehouse.model.Batch;
import java.util.List;

public record LoadResult(List<Batch> loaded, List<LoadError> errors) {
    public boolean hasErrors() { return !errors.isEmpty(); }
}