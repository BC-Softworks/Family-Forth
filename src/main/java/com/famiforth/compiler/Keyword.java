package com.famiforth.compiler;

import java.util.Arrays;

/**
 * Keywords are not defined in the dictionary 
 * but instead are handle directly by the parser
 */
public enum Keyword{
    COLON(":"),
    SEMICOLON(";");

    public final String value;

    private Keyword(String value) {
        this.value = value;
    }

    public static Keyword getByValue(String value){
        return Arrays.stream(Keyword.values()).filter(key -> key.value.equals(value)).findFirst().orElse(null);
    }
    
    public static boolean isKeyword(String str){
        return Arrays.stream(Keyword.values()).anyMatch(v -> v.value.equals(str));
    }
}