package com.famiforth.compiler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.famiforth.compiler.Lexer.Keyword;
import com.famiforth.compiler.Lexer.Token;
import com.famiforth.compiler.Lexer.TokenType;
import com.famiforth.dictionary.Definition;
import com.famiforth.dictionary.UserDictionary;
import com.famiforth.exceptions.SyntaxErrorException;

/** FamilyForth Parser
 * @author Edward Conn
*/
public class Parser {

    final private Lexer lexer;

    public Parser(Lexer scan, String dictionaryFile) {
        UserDictionary.initalize(dictionaryFile);
        this.lexer = scan;
    }

    public boolean hasNext() {
        return lexer != null && lexer.hasNext();
    } 

    public List<String> parse() throws IOException {
        // Return null if out of tokens to parse
        if(!lexer.hasNext()){
            return null;
        }

        Token token = lexer.next_token();
        // TokensTypes that return an empty list
        if(Lexer.TokenType.SKIP_LINE.equals(token.type)){
            lexer.skipLine();
            return List.of();
        } else if (Lexer.TokenType.BEGIN_COMMENT.equals(token.type)){
            while (lexer.next_token().type != TokenType.END_COMMENT);
            return List.of();
        } else if(Lexer.TokenType.KEYWORD.equals(token.type) && Keyword.getByValue(token.value).equals(Keyword.COLON)){
            parseColonStatement();
            return List.of();
        }
        
        // TokensTypes that return a populated list
        if(Lexer.TokenType.WORD.equals(token.type)){
            return parseWord(token);
        } else if(Lexer.TokenType.INTEGER.equals(token.type)){
            return parseInteger(token);
        }
        
        // TokensTypes that result in an exception
        if(Lexer.TokenType.FLOAT.equals(token.type)){
            throw new SyntaxErrorException("Floating point numbers are currently unsupported.");
        }

        throw new SyntaxErrorException(token.value, token.lineNumber);
    }

    /**
     * Get the Token's Definition from the dictionary
     * Throws an error if no Definition is found
     * @param token
     * @return 
     */
    private List<String> parseWord(Token token) {
        return UserDictionary.getDefinition(token.value).flattenDefinition();
    }

    // TOD: Create custom anonymous definitions for integers
    // TOD: Allow 32 bit values
    private List<String> parseInteger(Token token){
        return UserDictionary.getIntegerDefinition(token.value).flattenDefinition();
    }

    /**
     * ( C: "<spaces>name" -- colon-sys )
     * Skip leading space delimiters. Parse name delimited by a space. 
     * Create a definition for name, called a "colon definition".
     * Enter compilation state and start the current definition, producing colon-sys.
     * Append the initiation semantics given below to the current definition.
     * The execution semantics of name will be determined by the words compiled into the body of the definition. 
     * The current definition shall not be findable in the dictionary until it is ended (or until the execution of DOES> in some systems). 
     * 
     * @return
     * @throws IOException
     */
    private void parseColonStatement() throws IOException {
        String name = lexer.next_token().value;
        if (Keyword.getByValue(name) == Keyword.SEMICOLON) {
            throw new SyntaxErrorException("Empty definition");
        }

        List<String> wordList = new LinkedList<>();
        Token token = lexer.next_token();
        while (Keyword.getByValue(token.value) != Keyword.SEMICOLON) {
            wordList.add(token.value);
            token = lexer.next_token();
        }

        // Add the word to the dictionary
        UserDictionary.addWord(Definition.createUserWordDefinition(name, wordList));
    }
}
