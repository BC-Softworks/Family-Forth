package com.famiforth.lexer;

import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import com.famiforth.compiler.CompilerUtils;
import com.famiforth.lexer.LexerToken.TokenType;

/** FamilyForth Lexer
 * @author Edward Conn
 * @since v0.1
*/
public class Lexer {

    private static Scanner scanner;
    private static Queue<String> currentLine;
    private static String str_token;
    
    // Total number of tokens lexed
    private static int tokenNumber;
    // Total of lines lexed
    private static int lineNumber;

	public Lexer(Reader input) {
		scanner = new Scanner(input);
        str_token = "";
        currentLine = new LinkedList<>();
        tokenNumber = 0;
        lineNumber = 0;
	}

    public void skipLine(){
        currentLine.clear();
    }

    public boolean hasNext() {
        return !currentLine.isEmpty() || scanner.hasNext();
    } 

    /**
     * @return True if there is a remaining line
     * @throws IllegalStateException if this scanner is closed
     */
    public boolean hasNextLine() {
        return scanner.hasNextLine();
    } 

    /**
     * @return The Tokenized string provided by the Scanner
     */
    public LexerToken next_token() {
        advance();
        return new LexerToken(str_token, getType(), lineNumber, tokenNumber);
    }

    /**
     * Advance to the next token
     * This is done by line instead of 
     * directly grabbing next for easier debugging
     */
    protected static void advance() {
        if(currentLine.isEmpty()){
            if(scanner.hasNextLine()){
                String[] nextLine = StringUtils.split(scanner.nextLine());
                currentLine.addAll(Arrays.asList(nextLine));
                lineNumber += 1;
            } else {
                // Ensure the scanner has been closed
                // Then set str_token to null
                scanner.close();
                return;
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


    private TokenType getType() {
        TokenType type;
        if(Keyword.isKeyword(str_token)){
            type = TokenType.KEYWORD;
        } else if("\\".equals(str_token)){
            type = TokenType.SKIP_LINE;
        } else if("(".equals(str_token)){
            type = TokenType.BEGIN_COMMENT;
        } else if(")".equals(str_token)){
            type = TokenType.END_COMMENT;
        } else if(CompilerUtils.isInteger(str_token, 10)){
            type = TokenType.INTEGER;
        } else if(CompilerUtils.isFloat(str_token)){
            type = TokenType.FLOAT;
        } else {
            type = TokenType.WORD;
        }
        return type;
    }
}
