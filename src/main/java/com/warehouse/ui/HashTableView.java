package com.warehouse.ui;

import com.warehouse.structure.HashTable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Панель визуализации хеш-таблицы (Canvas).
 * Каждая ячейка массива - прямоугольник. Цвета:
 *   белый - FREE, голубой - занята, серый - удалена (DELETED),
 *   жёлтый - ячейка осмотрена в последней операции (путь проб),
 *   зелёный - итоговая ячейка (где нашли / куда вставили / что удалили).
 * Вызывайте render(...) после КАЖДОЙ операции - картинка перерисовывается целиком.
 */
public class HashTableView extends Canvas {

    private static final int COLUMNS = 8;
    private static final int MAX_DRAWN_SLOTS = 128;   // больше ячеек не рисуем (иначе не влезет)
    private static final int PATH_PREVIEW = 12;       // сколько индексов пути показывать текстом
    private static final double CELL_WIDTH = 64;
    private static final double CELL_HEIGHT = 30;
    private static final double MARGIN = 8;
    private static final double INFO_HEIGHT = 112;

    private static final Color COLOR_FREE = Color.WHITE;
    private static final Color COLOR_OCCUPIED = Color.LIGHTSKYBLUE;
    private static final Color COLOR_DELETED = Color.LIGHTGRAY;
    private static final Color COLOR_VISITED = Color.GOLD;
    private static final Color COLOR_RESULT = Color.LIMEGREEN;

    public HashTableView() {
        super(MARGIN * 2 + COLUMNS * CELL_WIDTH,
              INFO_HEIGHT + MARGIN + (MAX_DRAWN_SLOTS / COLUMNS) * CELL_HEIGHT);
    }

    /** Полная перерисовка. operation - подпись последней операции, например "get(1234)". */
    public void render(HashTable<?, ?> table, String operation) {
        GraphicsContext g = getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        int[] path = table.getLastPath();
        int result = table.getLastResultIndex();
        int drawn = Math.min(table.getCapacity(), MAX_DRAWN_SLOTS);

        // какие из рисуемых ячеек входят в путь проб
        boolean[] visited = new boolean[drawn];
        int hiddenVisited = 0;
        for (int index : path) {
            if (index < drawn) {
                visited[index] = true;
            } else {
                hiddenVisited++;
            }
        }

        drawInfo(g, table, operation, path, hiddenVisited, drawn);

        for (int i = 0; i < drawn; i++) {
            drawSlot(g, table, i, visited[i], i == result);
        }
    }

    private void drawInfo(GraphicsContext g, HashTable<?, ?> table, String operation,
                          int[] path, int hiddenVisited, int drawn) {
        g.setFill(Color.BLACK);
        g.setFont(Font.font(12));

        double y = 18;
        g.fillText("Операция: " + operation, MARGIN, y);
        y += 16;
        g.fillText("Ёмкость: %d   Элементов: %d   Удалённых: %d   Заполнение: %.2f".formatted(
                table.getCapacity(), table.size(), table.getDeletedCount(), table.getLoadFactor()), MARGIN, y);
        y += 16;
        g.fillText("Проб в операции: %d   Среднее за всё время: %.2f".formatted(
                table.getLastProbes(), table.getAverageProbes()), MARGIN, y);
        y += 16;
        g.fillText("Путь: " + pathToString(path) + (table.wasLastRehashed() ? "   [РЕХЕШ: ёмкость пересчитана]" : ""),
                MARGIN, y);
        y += 16;
        if (table.getCapacity() > drawn || hiddenVisited > 0) {
            g.fillText("Показаны первые %d ячеек из %d (в пути вне экрана: %d)".formatted(
                    drawn, table.getCapacity(), hiddenVisited), MARGIN, y);
        } else {
            g.fillText("Голубой - занято, серый - удалено, жёлтый - путь, зелёный - итог", MARGIN, y);
        }
    }

    private void drawSlot(GraphicsContext g, HashTable<?, ?> table, int i, boolean visited, boolean isResult) {
        double x = MARGIN + (i % COLUMNS) * CELL_WIDTH;
        double y = INFO_HEIGHT + (i / COLUMNS) * CELL_HEIGHT;

        HashTable.SlotState state = table.getSlotState(i);
        Color fill;
        if (isResult) {
            fill = COLOR_RESULT;
        } else if (visited) {
            fill = COLOR_VISITED;
        } else if (state == HashTable.SlotState.OCCUPIED) {
            fill = COLOR_OCCUPIED;
        } else if (state == HashTable.SlotState.DELETED) {
            fill = COLOR_DELETED;
        } else {
            fill = COLOR_FREE;
        }

        g.setFill(fill);
        g.fillRect(x, y, CELL_WIDTH, CELL_HEIGHT);
        g.setStroke(Color.BLACK);
        g.strokeRect(x, y, CELL_WIDTH, CELL_HEIGHT);

        g.setFill(Color.DIMGRAY);
        g.setFont(Font.font(9));
        g.fillText(String.valueOf(i), x + 3, y + 10);   // номер ячейки

        g.setFill(Color.BLACK);
        g.setFont(Font.font(11));
        if (state == HashTable.SlotState.OCCUPIED) {
            g.fillText(String.valueOf(table.getKeyAt(i)), x + 4, y + CELL_HEIGHT - 6);
        } else if (state == HashTable.SlotState.DELETED) {
            g.fillText("удалено", x + 4, y + CELL_HEIGHT - 6);
        }
    }

    private String pathToString(int[] path) {
        if (path.length == 0) {
            return "—";
        }
        StringBuilder sb = new StringBuilder();
        int shown = Math.min(path.length, PATH_PREVIEW);
        for (int i = 0; i < shown; i++) {
            if (i > 0) {
                sb.append(" → ");
            }
            sb.append(path[i]);
        }
        if (path.length > shown) {
            sb.append(" … (всего ").append(path.length).append(")");
        }
        return sb.toString();
    }
}
