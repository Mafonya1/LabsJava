package com.warehouse.warehouselab1;

import javafx.collections.ObservableList;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileService {

    public static int counter = 0;

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
        if (b instanceof ImportedBatch) {
            ImportedBatch imported = (ImportedBatch) b;
            return common + ";" + imported.getCountry() + ";" + imported.getCode();
        }
        return "Archive;" + common;
    }

    public static List<Batch> readerCSV(String fileP) throws IOException {
        List<Batch> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileP))) {
            // .lines() превращает reader в поток строк, который можно перебрать
            for (String line : (Iterable<String>) reader.lines()::iterator) {
                if (line.isBlank()) continue;
                try {
                    result.add(parseLine(line));
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    counter++;
                }
            }
        }
        return result;
    }

    private static Batch parseLine(String line) {
        String[] p = line.split(";");
        if (p[0].trim().equalsIgnoreCase("Archive")) {
            return new ArchivedBatch(Integer.parseInt(p[1].trim()), p[2].trim(),
                    Integer.parseInt(p[3].trim()), Integer.parseInt(p[4].trim()), p[5].trim());
        }
        return new ImportedBatch(Integer.parseInt(p[0].trim()), p[1].trim(),
                Integer.parseInt(p[2].trim()), Integer.parseInt(p[3].trim()), p[4].trim(),
                p[5].trim(), p[6].trim());
    }
}