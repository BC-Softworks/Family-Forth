package com.famiforth;

import java.io.IOException;
import java.io.Reader;
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

    public Pair<String, Integer> next_token() throws IOException {
        advance();
        return Pair.of(str_token, tokenNumber);
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
