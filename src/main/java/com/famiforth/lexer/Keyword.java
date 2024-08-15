package com.famiforth.lexer;

import java.util.Arrays;

/**
 * Keywords are not defined in the dictionary 
 * but instead are handle directly by the parser
 */
public enum Keyword{
    COLON(":"),
    SEMICOLON(";"),
    CODE("CODE"),
    ENDCODE("ENDCODE"),
    IF("IF"),
    ELSE("ELSE"),
    THEN("THEN"),
    DO("DO"),
    LOOP("LOOP"),
    PLUSLOOP("LOOP+"),
    LEAVE("LEAVE"),
    BEGIN("BEGIN"),
    WHILE("WHILE"),
    REPEAT("REPEAT"),
    RECURSE("RECURSE");


    public final String value;

    private Keyword(String value) {
        this.value = value;
    }

    public static Keyword getByValue(String value){
        return Arrays.stream(Keyword.values()).filter(key -> key.value.equalsIgnoreCase(value)).findFirst().orElse(null);
    }
    
    public static boolean isKeyword(String str){
        return Arrays.stream(Keyword.values()).anyMatch(v -> v.value.equalsIgnoreCase(str));
    }
}