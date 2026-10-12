package com.warehouse.warehouselab1;

import javafx.collections.ObservableList;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileService{
    private static final String ARCHIVE_MARK = "Archive";

    public static void saveCSV(String fileP, ObservableList<Batch> batches) throws IOException{
        try(BufferedWriter writen = new BufferedWriter(new FileWriter(fileP)))
        {
                for (Batch b : batches)
            {
                writen.write(toCsvLine(b));
                writen.newLine();
            }
        }
    }

    private static String toCsvLine(Batch b) {
        String common = b.getArticle() + ";" + b.getName() + ";" + b.getQt() + ";"
                + b.getCell() + ";" + b.getDate();

        if (b instanceof ImportedBatch imported) {
            // артикул;название;кол-во;ячейка;дата;страна;код
            return common + ";" + imported.getCountry() + ";" + imported.getCode();
        }
        // Archive;артикул;название;кол-во;ячейка;дата
        return ARCHIVE_MARK + ";" + common;
    }

    public static ReadRes.LoadResult readerCSV(String fileP) throws IOException {
        List<Batch> batches = new ArrayList<>();
        int skipped = 0;

        try(BufferedReader reader = new BufferedReader(new FileReader(fileP))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    batches.add(parseLine(line));
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    skipped++;                      // битая строка: пропускаем и считаем
                }
            }
            return new ReadRes.LoadResult(batches, skipped);
        }
    }

    private static Batch parseLine(String line) {
        String[] data = line.split(";");

        if (ARCHIVE_MARK.equalsIgnoreCase(data[0].trim())) {
            // Архивная партия: Archive;артикул;название;кол-во;ячейка;дата
            return new ArchivedBatch(
                    Integer.parseInt(data[1].trim()),
                    data[2].trim(),
                    Integer.parseInt(data[3].trim()),
                    Integer.parseInt(data[4].trim()),
                    data[5].trim());
        }
        // Импортная партия: артикул;название;кол-во;ячейка;дата;страна;код
        return new ImportedBatch(
                Integer.parseInt(data[0].trim()),
                data[1].trim(),
                Integer.parseInt(data[2].trim()),
                Integer.parseInt(data[3].trim()),
                data[4].trim(),
                data[5].trim(),
                data[6].trim());
    }
}