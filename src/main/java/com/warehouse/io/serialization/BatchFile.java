package com.warehouse.io.serialization;

import java.util.List;

/** Корень JSON-файла: { "batches": [ ... ] }. Список здесь - только форма файла, не хранилище. */
public record BatchFile(List<BatchDto> batches) { }
