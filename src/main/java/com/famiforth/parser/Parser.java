package com.famiforth.parser;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringJoiner;

import org.apache.commons.lang3.tuple.Pair;

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

    private final Lexer lexer;
    private final Deque<String> cfStack;


    private String wordName;
    private int ifCounter;
    private int doCounter;
    private int beginCounter;

    public Parser(Lexer scan, String dictionaryFile) {
        UserDictionary.initalize(dictionaryFile);
        this.lexer = scan;
        this.cfStack = new LinkedList<>();
        ifCounter = 0;
        doCounter = 0;
        beginCounter = 0;
    }

    public boolean hasNext() {
        return lexer != null && lexer.hasNext();
    } 

    /**
     * Parser the next vaild set of tokend from the lexer
     * @throws IOException
     */
    public ParserToken parse() throws IOException {
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
            // Except IMMEDIATE
            case KEYWORD:
                Definition def = null;
                DefinitionType type = null;
                Pair<String, String> reference = null;
                switch(Keyword.getByValue(token.value)) {
                    case COLON:
                        type = DefinitionType.COLON;
                        def = parseColonStatement(token);
                        break;
                    case IMMEDIATE:
                        // Get definition of last word.
                        // Set it to be an immediate word.
                        // Update the dictionary
                        UserDictionary.modifyUserDefinedWord(wordName);
                        return null;
                    case CODE:
                        type = DefinitionType.CODE;
                        def = parseCodeBlock(token);
                        System.out.println(def);
                        break;
                    case IF:
                        type = DefinitionType.IF;
                        def = getDefinition(token);
                        cfStack.add("if_" + ifCounter++);
                        reference = Pair.of(cfStack.getLast(), null);
                        break;
                    case ELSE:
                        type = DefinitionType.ELSE;
                        def = getDefinition(token);
                        String left = cfStack.pollLast();
                        cfStack.add("else_" + ifCounter++);
                        reference = Pair.of(left, cfStack.getLast());
                        break;
                    case THEN:
                        type = DefinitionType.THEN;
                        def = getDefinition(token);
                        reference = Pair.of(cfStack.pollLast(), null);
                        break;
                    case DO:
                        type = DefinitionType.DO;
                        def = getDefinition(token);
                        cfStack.add("do_" + doCounter++);
                        break;
                    case LOOP:
                        type = DefinitionType.LOOP;
                        def = getDefinition(token);
                        reference = Pair.of(cfStack.pollLast(), null);
                        break;
                    case PLUSLOOP:
                        type = DefinitionType.PLUSLOOP;
                        def = getDefinition(token);
                        reference = Pair.of(cfStack.pollLast(), null);
                        break;
                    case LEAVE:
                        type = DefinitionType.LEAVE;
                        def = getDefinition(token);
                        reference = Pair.of(cfStack.pollLast(), null);
                        break;
                    case BEGIN:
                        type = DefinitionType.BEGIN;
                        def = getDefinition(token);
                        cfStack.add("begin_" + beginCounter++);
                        break;
                    case WHILE:
                        type = DefinitionType.WHILE;
                        def = getDefinition(token);
                        reference = Pair.of(cfStack.pollLast(), null);
                        cfStack.add("while_" + beginCounter++);
                        break;
                    case REPEAT:
                        type = DefinitionType.REPEAT;
                        def = getDefinition(token);
                        reference = Pair.of(cfStack.pollLast(), null);
                        break;
                    case RECURSE:
                        type = DefinitionType.RECURSE;
                        def = getDefinition(token);
                        reference = Pair.of(cfStack.pollLast(), null);
                        break;
                    default:
                        throw new SyntaxErrorException("Compilation time word encountered out of order.");
                }
                return new ParserToken(def, type, reference, wordName);
            case WORD:
                return new ParserToken(getDefinition(token), DefinitionType.WORD);
            case INTEGER:
                return new ParserToken(UserDictionary.getIntegerDefinition(token.value), DefinitionType.INTEGER);
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
            if((Keyword.SEMICOLON).equals(Keyword.getByValue(token.value))){
                break;
            }
            wordList.add(token.value);
        }

        // Poll the name of the word
        wordName = wordList.poll();

        // Add the word to the dictionary
        UserDictionary.addUserDefinedWord(wordName, false, List.copyOf(wordList));

        // Return newly created Definition
        return UserDictionary.getDefinition(wordName);
    }

    private Definition parseCodeBlock(LexerToken lexerToken) throws IOException {
        LexerToken token = lexerToken;
        List<String> wordList = new LinkedList<>();
        int lineNumber = lexerToken.lineNumber;
        StringJoiner lineJoiner = new StringJoiner(" ");

        token = skipAssemblyComments(lexer.next_token());
        while(!(Keyword.ENDCODE).equals(Keyword.getByValue(token.value))) {
            if(lineNumber != token.lineNumber){
                lineNumber = token.lineNumber;
                wordList.add(lineJoiner.toString());
                lineJoiner = new StringJoiner(" ");
            }

            lineJoiner.add(token.value);
            token = skipAssemblyComments(lexer.next_token());
        }
        wordList.add(lineJoiner.toString());

        // Generate an anonymous definition for the code block
        return UserDictionary.getAnonymousDefinition(wordList);
    }

    private LexerToken skipComments(LexerToken token) {
        if((LexerToken.TokenType.BEGIN_COMMENT).equals(token.type)){
            while (!LexerToken.TokenType.END_COMMENT.equals(lexer.next_token().type));
            return lexer.next_token();
        }
        return token;
    }


    /**
     * Skip comments inside assembly blocks
     * @param token
     * @return Next token after the commented text
     */
    private LexerToken skipAssemblyComments(LexerToken token) {
        if(Keyword.SEMICOLON.equals(Keyword.getByValue(token.value))){
            lexer.skipLine();
        }
        return token;
    }
}
