package com.famiforth;

public class SyntaxErrorException extends RuntimeException
{
    public SyntaxErrorException() {}

    public SyntaxErrorException(String message) {
        super("Syntax Error: " + message);
    }

    public SyntaxErrorException(String message, int lineNumber) {
        super(String.format("Syntax Error on line number %d: %s", lineNumber, message));
    }
}