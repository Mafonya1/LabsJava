package com.warehouse.io.serialization.envelopes;

import com.warehouse.model.Batch;
import com.warehouse.structure.HashTable;

import java.io.Serializable;

/**
 * Обертка для BIN-файла: явная версия схемы + данные.
 * Нужна для симметрии с JSON и для будущих миграций.
 */
public class BinaryEnvelope implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int CURRENT_VERSION = 1;

    private final int schemaVersion;
    private final HashTable<Integer, Batch> table;

    public BinaryEnvelope(int schemaVersion, HashTable<Integer, Batch> table) {
        this.schemaVersion = schemaVersion;
        this.table = table;
    }

    public int getSchemaVersion() { return schemaVersion; }
    public HashTable<Integer, Batch> getTable() { return table; }
}