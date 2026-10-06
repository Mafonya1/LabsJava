package com.warehouse.warehouselab1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileService{

    public static List<Batch> readerCSV(String fileP){
        List<Batch> spisok = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(fileP))) {
            String s;//читаем построчно
            while ((s = br.readLine()) != null) {
                if (s.isEmpty()) {
                    continue;
                }
                try {
                    // Разделяем по точке с запятой, как у вас в файле batches.csv
                    String[] data = s.split(";");

                    // Считываем тип строки (например, Archive или 102)
                    String typeOrArticle = data[0].trim();

                    if ("Archive".equalsIgnoreCase(typeOrArticle)) {
                        // Парсим архивную партию (в ней 6 элементов)
                        int article = Integer.parseInt(data[1].trim());
                        String police = data[2].trim();
                        int qt = Integer.parseInt(data[3].trim());
                        int cell = Integer.parseInt(data[4].trim());
                        String date = data[5].trim();

                        // Добавляем именно в list
                        spisok.add(new ArchivedBatch(article, police, qt, cell, date));

                    } else {
                        // Парсим импортную партию (если первая ячейка — это сразу число-артикул)
                        int article = Integer.parseInt(typeOrArticle);
                        String name = data[1].trim();
                        int qt = Integer.parseInt(data[2].trim());
                        int cell = Integer.parseInt(data[3].trim());
                        String date = data[4].trim();
                        String country = data[5].trim();
                        String code = data[6].trim();

                        // Добавляем именно в list
                        spisok.add(new ImportedBatch(article, name, qt, cell, date, country, code));
                    }

                } catch (Exception e) { //если строка битая, то просто ее пропускаем
                    System.out.println("Некорректная строка: " + s + " Ошибка: " + e.getMessage());
                }
            }
        }catch(IOException e){
            System.out.println(e.getMessage());
        }return spisok;
    }

    public static void main(String[] args){
        String fileP = "/home/maria/Документы/НГТУ ресурсы/3 семестр/Java/WarehouseLab1/batches.csv ";
        List<Batch> test = FileService.readerCSV(fileP);

        System.out.println("успешно");
    }
}
