package com.warehouse.io.serialization.envelopes;

import com.warehouse.model.Batch;
import java.util.List;

/**
 * Обертка для JSON-файла: версия схемы + данные.
 * Нужна для поддержки миграций в будущем (лаба 4).
 */
public record JsonEnvelope(int schemaVersion, List<Batch> items) {
    public static final int CURRENT_VERSION = 1;
}