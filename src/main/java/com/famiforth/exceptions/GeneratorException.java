package com.famiforth.exceptions;

public class GeneratorException extends RuntimeException {
    public GeneratorException() {}

    public GeneratorException(String message) {
        super("Generator Error: " + message);
    }

    public GeneratorException(String message, int lineNumber) {
        super(String.format("Generator Error on line number %d: %s", lineNumber, message));
    }
}
