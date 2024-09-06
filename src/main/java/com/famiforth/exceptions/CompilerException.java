package com.famiforth.exceptions;

public class CompilerException extends RuntimeException {
    public CompilerException() {}

    public CompilerException(String message, Exception ex) {
        super("Compiler Exception: " + message, ex);
    }

    public CompilerException(String message) {
        super("Compiler Exception: " + message);
    }
}