package com.famiforth;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class Lexer {

    private static Scanner scanner;
    private static int tokenNumber;
    private static String str_token;


	public Lexer(Reader input) {
		scanner = new Scanner(input);
        tokenNumber = 0;
	}

    /**
     * Advance to the next token
     * @throws IOException
     */
    protected static void advance() throws IOException  {
        if(scanner.hasNext()){
            str_token = scanner.next();
            str_token = str_token.toUpperCase();
            tokenNumber += 1;
        } else {
            scanner.close();
        }
	}

    public boolean hasNext() {
        return scanner.hasNext();
    } 

    /**
     * 
     * @return The next token pair
     * @throws IOException
     */
    public Pair<String, TokenType> next_token() throws IOException {
        advance();

        TokenType type;
        if(isKeyword(str_token)){
            type = TokenType.KEYWORD;
        } else if(isInteger(str_token, 10)){
            type = TokenType.INTEGER;
        } else {
            type = TokenType.WORD;
        }
        
        return Pair.of(str_token, type);
    }


    // Lexme type checks

    public enum TokenType{
        KEYWORD,
        INTEGER,
        WORD
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

    public static boolean isInteger(String s, int radix) {
        // Reject empty Strings
        if(StringUtils.isBlank(s)){
            return false;
        }

        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && (s.charAt(i) == '-' || s.charAt(i) == '+')) {
                // Ignore leading sign unless it is the only symbol
                if(s.length() == 1) {
                    return false;
                }
            } else if(Character.digit(s.charAt(i),radix) < 0){
                return false;
            }
        }
        return true;
    }
}
