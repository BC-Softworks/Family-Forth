package com.famiforth.compiler;

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
        } else if(LexerUtils.isInteger(str_token, 10)){
            type = TokenType.INTEGER;
        } else if(LexerUtils.isFloat(str_token)){
            type = TokenType.FLOAT;
        } else {
            type = TokenType.WORD;
        }
        
        return new Token(str_token, type, lineNumber, tokenNumber);
    }

    // Define Token and token types
    public class Token {
        public String value;
        public TokenType type;
        protected int lineNumber;
        protected int tokenNumber;

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
    }

    public static boolean isKeyword(String str){
        return Arrays.stream(Keyword.values()).anyMatch(v -> v.value.equals(str));
    }
}
