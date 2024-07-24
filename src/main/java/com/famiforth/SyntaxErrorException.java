package com.famiforth;

public class SyntaxErrorException extends RuntimeException
{
    public SyntaxErrorException() {}

    public SyntaxErrorException(String message) {
        super("Syntax Error: " + message);
    }
}