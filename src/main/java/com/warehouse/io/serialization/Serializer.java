package com.warehouse.io.serialization;

import com.warehouse.model.Batch;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Общий контракт сериализации. GUI и тесты работают только через этот интерфейс
 * и не знают, что внутри: JSON, бинарный формат или что-то ещё.
 * Главное свойство (roundtrip): load(save(x)) возвращает данные, равные x.
 */
public interface Serializer {

    /** Короткое имя формата. Совпадает с расширением файла: "json", "bin". */
    String format();

    void save(Batch[] batches, Path file) throws IOException;

    Batch[] load(Path file) throws IOException;
}
