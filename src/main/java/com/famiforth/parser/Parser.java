package com.famiforth.parser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.famiforth.exceptions.SyntaxErrorException;
import com.famiforth.lexer.Keyword;
import com.famiforth.lexer.Lexer;
import com.famiforth.lexer.LexerToken;
import com.famiforth.parser.ParserToken.DefinitionType;
import com.famiforth.parser.dictionary.Definition;
import com.famiforth.parser.dictionary.UserDictionary;

/** FamilyForth Parser
 * @author Edward Conn
*/
public class Parser {

    final private Lexer lexer;

    // Total number of IF Keywords encountered
    private int ifCounter;
    // Offset used for nested control flow statements
    private int labelOffset;

    public Parser(Lexer scan, String dictionaryFile) {
        UserDictionary.initalize(dictionaryFile);
        this.lexer = scan;
        ifCounter = 0;
        labelOffset = -1;
    }

    public boolean hasNext() {
        return lexer != null && lexer.hasNext();
    } 

    /**
     * Parser the next vaild set of tokend from the lexer
     * @throws IOException 
     */
    public ParserToken parse() throws IOException {
        // Return null if out of tokens to parse
        if(!lexer.hasNext()){
            return null;
        }

        LexerToken token = lexer.next_token();
        // TokensTypes that return null

        switch(token.type){
            case SKIP_LINE:
                lexer.skipLine();
                return null;
            case BEGIN_COMMENT:
                while (lexer.next_token().type != LexerToken.TokenType.END_COMMENT);
                return null;
            // TokensTypes that return a ParserToken
            case KEYWORD:
                Keyword key = Keyword.getByValue(token.value);

                Definition def = null;
                DefinitionType type = null;
                if(key.equals(Keyword.COLON)){
                    type = DefinitionType.COLON;
                    def = parseColonStatement();
                } else if(key.equals(Keyword.IF)){
                    ifCounter++;
                    labelOffset++;
                    type = DefinitionType.IF;
                    def = getDefinition(token);
                } else if(key.equals(Keyword.ELSE)){
                    labelOffset--;
                    type = DefinitionType.ELSE;
                    def = getDefinition(token);
                } else if(key.equals(Keyword.THEN)){
                    labelOffset--;
                    type = DefinitionType.THEN;
                    def = getDefinition(token);
                }
                return new ParserToken(def, type, ifCounter + labelOffset);
            case WORD:
                return new ParserToken(getDefinition(token), DefinitionType.WORD, ifCounter + labelOffset);
            case INTEGER:
                return new ParserToken(UserDictionary.getIntegerDefinition(token.value), DefinitionType.INTEGER, ifCounter + labelOffset);
            // TokensTypes that result in an exception
            case END_COMMENT:
                throw new SyntaxErrorException("A comment can not be closed if it was not opened");
            case FLOAT:
                throw new SyntaxErrorException("Floating point numbers are currently unsupported.");
        
        }
        throw new SyntaxErrorException(token.value, token.lineNumber);
    }

    /**
     * Get the Token's Definition from the dictionary
     * Throws an error if no Definition is found
     * @param token
     * @throws SyntaxErrorException
     */
    private Definition getDefinition(LexerToken token) {
        Definition def = UserDictionary.getDefinition(token.value);
        if(def == null){
            throw new SyntaxErrorException(String.format("Unable to find %s in the dictionary.", token.value), token.lineNumber);
        }
        return def;
    }

    private Definition parseColonStatement() throws IOException {
        String name = lexer.next_token().value;
        if (Keyword.getByValue(name) == Keyword.SEMICOLON) {
            throw new SyntaxErrorException("Empty definition");
        }
        
        List<String> wordList = new LinkedList<>();
        LexerToken token = lexer.next_token();
        while(Keyword.getByValue(token.value) != Keyword.SEMICOLON) {
            wordList.add(token.value);
            token = lexer.next_token();
        }

        // Add the word to the dictionary
        UserDictionary.addUserDefinedWord(name, false, wordList);

        return UserDictionary.getDefinition(name);
    }
}
