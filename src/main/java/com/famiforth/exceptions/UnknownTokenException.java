package com.famiforth.exceptions;

public class UnknownTokenException extends RuntimeException {
    public UnknownTokenException() {}

    public UnknownTokenException(String message) {
        super("Unknown Token: " + message);
    }

    public UnknownTokenException(String message, int lineNumber) {
        super(String.format("Unknown Token on line number %d: %s", lineNumber, message));
    }
}