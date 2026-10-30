package com.warehouse.ui;

import com.warehouse.structure.HashTable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Панель, на которой рисуется хеш-таблица. Каждая ячейка - прямоугольник.
 * Цвета: белый - свободна, голубой - занята, серый - удалена,
 *        жёлтый - ячейка осмотрена в последней операции, зелёный - итоговая ячейка.
 * Метод draw() вызывается после КАЖДОЙ операции и рисует всё заново.
 */
public class HashTableView extends Canvas {

    private static final int COLUMNS = 8;           // ячеек в строке
    private static final int MAX_CELLS = 128;       // больше ячеек не рисуем
    private static final double CELL_WIDTH = 64;
    private static final double CELL_HEIGHT = 30;
    private static final double TOP = 100;          // сверху место под текст

    public HashTableView() {
        super(COLUMNS * CELL_WIDTH + 16, TOP + (MAX_CELLS / COLUMNS) * CELL_HEIGHT + 8);
    }

    public void draw(HashTable<?, ?> table, String operation) {
        GraphicsContext g = getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        int[] path = table.getLastPath();
        int cells = Math.min(table.getCapacity(), MAX_CELLS);

        // какие из рисуемых ячеек вошли в путь проб
        boolean[] visited = new boolean[MAX_CELLS];
        for (int i = 0; i < path.length; i++) {
            if (path[i] < MAX_CELLS) {
                visited[path[i]] = true;
            }
        }

        // текст сверху
        double average = 0;
        if (table.getOperationCount() > 0) {
            average = (double) table.getTotalProbes() / table.getOperationCount();
        }
        String pathText = "";
        for (int i = 0; i < path.length && i < 10; i++) {
            pathText = pathText + path[i] + " ";
        }
        if (path.length > 10) {
            pathText = pathText + "... (всего " + path.length + ")";
        }

        g.setFill(Color.BLACK);
        g.setFont(Font.font(12));
        g.fillText("Операция: " + operation, 8, 18);
        g.fillText("Ёмкость: " + table.getCapacity() + "   Элементов: " + table.size()
                + "   Удалённых: " + table.getDeletedCount(), 8, 36);
        g.fillText("Проб в операции: " + path.length + "   Среднее: " + String.format("%.2f", average), 8, 54);
        String rehashText = "";
        if (table.wasLastRehashed()) {
            rehashText = "   [был рехеш]";
        }
        g.fillText("Путь: " + pathText + rehashText, 8, 72);
        if (table.getCapacity() > MAX_CELLS) {
            g.fillText("Показаны первые " + MAX_CELLS + " ячеек из " + table.getCapacity(), 8, 90);
        } else {
            g.fillText("Голубой - занято, серый - удалено, жёлтый - путь, зелёный - итог", 8, 90);
        }

        // ячейки
        for (int i = 0; i < cells; i++) {
            double x = 8 + (i % COLUMNS) * CELL_WIDTH;
            double y = TOP + (i / COLUMNS) * CELL_HEIGHT;

            Color color = Color.WHITE;
            if (table.getSlotState(i) == HashTable.SlotState.OCCUPIED) {
                color = Color.LIGHTSKYBLUE;
            }
            if (table.getSlotState(i) == HashTable.SlotState.DELETED) {
                color = Color.LIGHTGRAY;
            }
            if (visited[i]) {
                color = Color.GOLD;
            }
            if (i == table.getLastResultIndex()) {
                color = Color.LIMEGREEN;
            }

            g.setFill(color);
            g.fillRect(x, y, CELL_WIDTH, CELL_HEIGHT);
            g.setStroke(Color.BLACK);
            g.strokeRect(x, y, CELL_WIDTH, CELL_HEIGHT);

            g.setFill(Color.DIMGRAY);
            g.setFont(Font.font(9));
            g.fillText(String.valueOf(i), x + 3, y + 10);          // номер ячейки

            g.setFill(Color.BLACK);
            g.setFont(Font.font(11));
            if (table.getSlotState(i) == HashTable.SlotState.OCCUPIED) {
                g.fillText(String.valueOf(table.getKeyAt(i)), x + 4, y + CELL_HEIGHT - 6);
            }
            if (table.getSlotState(i) == HashTable.SlotState.DELETED) {
                g.fillText("удалено", x + 4, y + CELL_HEIGHT - 6);
            }
        }
    }
}
