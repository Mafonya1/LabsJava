package com.warehouse.io;

public class CsvLoadException extends RuntimeException {

    private final LoadError.ErrorCode code;

    public CsvLoadException(LoadError.ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public LoadError.ErrorCode getCode() {
        return code;
    }
}