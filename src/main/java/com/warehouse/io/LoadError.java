package com.warehouse.io;

public record LoadError(int lineNumber, String line, ErrorCode code, String message) {
    public enum ErrorCode { BAD_NUMBER, WRONG_FIELD_COUNT, UNKNOWN_TYPE, EMPTY_LINE }
}