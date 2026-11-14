package com.warehouse.io;

import java.util.ArrayList;
import java.util.List;

public final class CsvFormat {

    public static final char DELIMITER = ';';
    public static final String HEADER = "type;article;name;quantity;cell;arrivalDate;country;customsCode";
    public static final int COLUMNS = 8;

    public static final String TYPE_IMPORTED = "imported";
    public static final String TYPE_ARCHIVED = "archived";

    private CsvFormat() { }

    /** Готовит значение для записи: при необходимости берёт в кавычки. */
    public static String escape(String value) {
        String text = value.replace('\r', ' ').replace('\n', ' ');
        if (text.indexOf(DELIMITER) >= 0 || text.indexOf('"') >= 0) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    /** Режет строку на поля с учётом кавычек. **/
    public static String[] split(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    boolean doubled = i + 1 < line.length() && line.charAt(i + 1) == '"';
                    if (doubled) {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;            // закрывающая кавычка
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"' && current.length() == 0) {
                inQuotes = true;                     // кавычка в начале поля
            } else if (c == DELIMITER) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (inQuotes) {
            throw new CsvLoadException(LoadError.ErrorCode.BAD_QUOTES, "Не закрыта кавычка");
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}
