package com.famiforth;

import java.util.Arrays;
import java.util.List;

public class Definition {
    final boolean isPrimitive;
    final String name;
    final List<String> words;


    public Definition(boolean isPrimitive, String name) {
        this.isPrimitive = isPrimitive;
        this.name = name;
        this.words = null;
    }

    public Definition(boolean isPrimitive, String name, List<String> words) {
        this.isPrimitive = isPrimitive;
        this.name = name;
        this.words = words;
    }

    @Override
    public String toString() {
        return String.format("Definition [%0: %1]", name, words);
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public String getName() {
        return name;
    }

    public List<String> getWords() {
        return words;
    }

    public enum Keyword{
        COLON(":"),
        SEMICOLON(";"),
        END("END"),
        IF("IF"),
        ELSE("ELSE"),
        THEN("THEN"),
        BEGIN("BEGIN"),
        WHILE("WHILE"),
        REPEAT("REPEAT"),
        UNTIL("UNTIL"),
        DO("DO"),
        LOOP("LOOP"),
        PLUS_LOOP("+LOOP");

        public final String value;

        private Keyword(String value) {
            this.value = value;
        }
    }

    public static boolean isKeyword(String str){
        return Arrays.stream(Keyword.values()).anyMatch(v -> v.value.equals(str));
    }
}
