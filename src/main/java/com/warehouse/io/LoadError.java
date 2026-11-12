package com.warehouse.io;

public record LoadError(int lineNumber, String line, ErrorCode code, String message) {

    public enum ErrorCode {
        BAD_HEADER,          // первая строка - не наш заголовок (например, файл в старом формате)
        WRONG_FIELD_COUNT,   // не 8 колонок
        BAD_NUMBER,          // в числовом поле не число
        UNKNOWN_TYPE,        // в колонке type не imported и не archived
        BAD_QUOTES,          // не закрыта кавычка
        FILE_ERROR           // файла нет или его не удалось прочитать
    }
}