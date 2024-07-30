package com.famiforth;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

/** FamilyForth Lexer
 * @author Edward Conn
*/
public class Lexer {

    private static Scanner scanner;
    private static Queue<String> currentLine;
    private static String str_token;


    private static int tokenNumber;
    private static int lineNumber;

	@SuppressWarnings("unused")
    private Lexer() {}

	public Lexer(Reader input) {
		scanner = new Scanner(input);
        str_token = "";
        currentLine = new LinkedList<>();
        tokenNumber = 0;
        lineNumber = 0;
	}

    /**
     * Advance to the next token
     * This is done by line instead of 
     * directly grabbing next for easier debugging
     * @throws IOException
     */

    protected static void advance() throws IOException  {
        if(currentLine.isEmpty()){
            if(scanner.hasNextLine()){
                String[] nextLine = StringUtils.split(scanner.nextLine());
                currentLine.addAll(Arrays.asList(nextLine));
                lineNumber += 1;
            } else {
                // Ensure the scanner has been closed
                // Then set str_token to null
                scanner.close();
            }
        }

        // If currentLine is populated
        // set str_token to the head
        str_token = currentLine.poll();
        // Remove all non ASCII Characters
        str_token = str_token.replaceAll("[^\\x00-\\x7F]", "");
        str_token = str_token.toUpperCase();
        tokenNumber += 1;
	}

    public void skipLine(){
        currentLine.clear();
    }

    public boolean hasNext() {
        return !currentLine.isEmpty() || scanner.hasNext();
    } 

    public boolean hasNextLine() {
        return scanner.hasNextLine();
    } 

    /**
     * 
     * @return The next Token object
     * @throws IOException
     */
    public Token next_token() throws IOException {
        advance();

        TokenType type;
        if(isKeyword(str_token)){
            type = TokenType.KEYWORD;
        } else if("\\".equals(str_token)){
            type = TokenType.SKIP_LINE;
        } else if("(".equals(str_token)){
            type = TokenType.BEGIN_COMMENT;
        } else if(")".equals(str_token)){
            type = TokenType.END_COMMENT;
        } else if(isInteger(str_token, 10)){
            type = TokenType.INTEGER;
        } else if(isFloat(str_token)){
            type = TokenType.FLOAT;
        } else {
            type = TokenType.WORD;
        }
        
        return new Token(str_token, type, lineNumber, tokenNumber);
    }

    // Define Token and token types
    public class Token {
        String value;
        TokenType type;
        int lineNumber;
        int tokenNumber;

        public Token(String value, TokenType type, int lineNumber, int tokenNumber) {
            this.value = value;
            this.type = type;
            this.lineNumber = lineNumber;
            this.tokenNumber = tokenNumber;
        }
    }


    public enum TokenType{
        SKIP_LINE,
        BEGIN_COMMENT,
        END_COMMENT,
        KEYWORD,
        FLOAT,
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
        PLUS_LOOP("+LOOP"),
        CASE("CASE"),
        OF("OF"),
        ENDOF("ENDOF"),
        ENDCASE("ENDCASE"),
        AGAIN("AGAIN");

        public final String value;

        private Keyword(String value) {
            this.value = value;
        }

        public static Keyword getByValue(String value){
            return Arrays.stream(Keyword.values()).filter(key -> key.value.equals(value)).findFirst().orElse(null);
        }
    }

    public static boolean isKeyword(String str){
        return Arrays.stream(Keyword.values()).anyMatch(v -> v.value.equals(str));
    }

    /**
     * Check if a string is an integer
     * Max an min values are not checked
     * Bounds checkign is left up to the parser
     * @param s
     * @param radix
     * @return boolean
     */
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
            } else if(Character.digit(s.charAt(i), radix) < 0){
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a string is an floating point number
     * These are not supported and are rejected by the parser
     * @param s
     * @param radix
     * @return boolean
     */
    public static boolean isFloat(String s) {
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
            } else if(!(Character.isDigit(s.charAt(i)) || s.charAt(i) == '.')){
                return false;
            }
        }
        return true;
    }
}
