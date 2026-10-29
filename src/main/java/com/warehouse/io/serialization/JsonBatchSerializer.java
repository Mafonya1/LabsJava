package com.warehouse.io.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.warehouse.model.Batch;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** JSON через Jackson. INDENT_OUTPUT - файл с отступами, его удобно читать глазами. */
public final class JsonBatchSerializer implements Serializer {

    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public String format() {
        return "json";
    }

    @Override
    public void save(Batch[] batches, Path file) throws IOException {
        List<BatchDto> items = new ArrayList<>(batches.length);
        for (Batch b : batches) {
            items.add(BatchDto.from(b));
        }
        mapper.writeValue(file.toFile(), new BatchFile(items));
    }

    @Override
    public Batch[] load(Path file) throws IOException {
        BatchFile data = mapper.readValue(file.toFile(), BatchFile.class);
        if (data == null || data.batches() == null) {
            throw new IOException("В файле нет списка batches");
        }

        Batch[] result = new Batch[data.batches().size()];
        int number = 0;
        for (BatchDto dto : data.batches()) {
            number++;
            if (dto == null) {
                throw new IOException("Запись №" + number + ": пустой элемент");
            }
            try {
                result[number - 1] = dto.toBatch();
            } catch (IllegalArgumentException e) {
                throw new IOException("Запись №" + number + ": " + e.getMessage(), e);
            }
        }
        return result;
    }
}
