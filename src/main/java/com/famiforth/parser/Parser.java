package com.famiforth.parser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

    // Total number of DO Keywords encountered
    private int doCounter;

    public Parser(Lexer scan, String dictionaryFile) {
        UserDictionary.initalize(dictionaryFile);
        this.lexer = scan;
        ifCounter = 0;
        doCounter = 0;
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
                while (!LexerToken.TokenType.END_COMMENT.equals(lexer.next_token().type));
                return null;
            // TokensTypes that return a ParserToken
            case KEYWORD:
                Keyword key = Keyword.getByValue(token.value);

                Definition def = null;
                DefinitionType type = null;
                if(key.equals(Keyword.COLON)){
                    type = DefinitionType.COLON;
                    def = parseColonStatement(token);
                } else if(key.equals(Keyword.IF)){
                    ifCounter++;
                    type = DefinitionType.IF;
                    def = getDefinition(token);
                } else if(key.equals(Keyword.ELSE)){
                    type = DefinitionType.ELSE;
                    def = getDefinition(token);
                } else if(key.equals(Keyword.THEN)){
                    type = DefinitionType.THEN;
                    def = getDefinition(token);
                } else if(key.equals(Keyword.DO)){
                    doCounter++;
                    type = DefinitionType.DO;
                    def = getDefinition(token);
                } else if(key.equals(Keyword.LOOP)){
                    type = DefinitionType.LOOP;
                    def = getDefinition(token);
                }
                return new ParserToken(def, type, ifCounter, doCounter);
            case WORD:
                return new ParserToken(getDefinition(token), DefinitionType.WORD, ifCounter, doCounter);
            case INTEGER:
                return new ParserToken(UserDictionary.getIntegerDefinition(token.value), DefinitionType.INTEGER, ifCounter, doCounter);
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

    private Definition parseColonStatement(LexerToken lexerToken) throws IOException {
        LexerToken token = lexerToken;
        Queue<String> wordList = new LinkedList<>();
        while(true) {
            token = skipComments(lexer.next_token());
            if(!(Keyword.SEMICOLON).equals(Keyword.getByValue(token.value))){
                wordList.add(token.value);
            } else {
                break;
            }
        }

        // Poll the name of the word
        String name = wordList.poll();

        // Add the word to the dictionary
        UserDictionary.addUserDefinedWord(name, false, List.copyOf(wordList));

        // Return newly created Definition
        return UserDictionary.getDefinition(name);
    }

    private LexerToken skipComments(LexerToken token) {
        if((LexerToken.TokenType.BEGIN_COMMENT).equals(token.type)){
            while (!LexerToken.TokenType.END_COMMENT.equals(lexer.next_token().type));
            return lexer.next_token();
        }
        return token;
    }
}
