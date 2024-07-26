package com.famiforth;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.famiforth.Lexer.Keyword;
import com.famiforth.Lexer.Token;
import com.famiforth.Lexer.TokenType;

public class Parser {

    final private Lexer lexer;
    final private UserDictionary dictionary;

    final File fileOut;
    final private List<List<String>> parsedWords = new LinkedList<>();
    
    final static String DEFAULT_FILE_OUT = "out.asm";

    public Parser(Lexer scan, String dictionaryFile, String fileName) {
        this.lexer = scan;
        this.dictionary = UserDictionary.getInstance(dictionaryFile);
        fileOut = new File(fileName);
    }

    public Parser(Lexer scan, String dictionaryFile) {
        this(scan, dictionaryFile, DEFAULT_FILE_OUT);
    }

    public void parse() throws IOException{
        while(lexer.hasNext()){
            List<String> procudureList = new ArrayList<>();

            Token token = lexer.next_token();
            switch(token.type){
                case SKIP_LINE:
                    lexer.skipLine();
                    continue;
                case BEGIN_COMMENT:
                    while(token.type != TokenType.END_COMMENT){
                        token = lexer.next_token();
                    }
                    continue;
                case KEYWORD:
                    procudureList = parseKeyword(Keyword.valueOf(token.value));
                    break;
                case FLOAT:
                    throw new SyntaxErrorException("Floating point numbers are currently unsupported.");
                case INTEGER:
                    // TODO: Add proc to push to top
                    procudureList = List.of(token.value);
                    break;
                case WORD:
                    Definition word = dictionary.getDefinition(token.value);
                    procudureList = expandDefinition(word);
                    break;
                default:
                    break;
            }
            
            // Used for debugging
            // Disable after tests are implemented
            parsedWords.add(procudureList);

            //TODO: Write to file instead of System.out
            System.out.println(StringUtils.join(procudureList, System.lineSeparator()));
        }
    }

    public List<String> parseKeyword(Keyword keyword){
        switch (keyword) {
            case BEGIN:
                return parseBeginStatement();
            case COLON:
                return parseColonStatement();
            case IF:
                return parseIfStatement();
            case LOOP:
            case PLUS_LOOP:
                return parseLoop();
        
            default:
                throw new IllegalArgumentException(String.format("Syntax Error: Keyword %s encountered out of order.", keyword.value));
        }
    }

    private List<String> parseBeginStatement() {
        throw new UnsupportedOperationException("Unimplemented method 'parseBeginStatement'");
    }

    private List<String> parseColonStatement() {
        throw new UnsupportedOperationException("Unimplemented method 'parseColonStatement'");
    }

    private List<String> parseIfStatement() {
        throw new UnsupportedOperationException("Unimplemented method 'parseIfStatement'");
    }

    private List<String> parseLoop() {
        throw new UnsupportedOperationException("Unimplemented method 'parseLoop'");
    }

    public List<String> expandDefinition(Definition definition){
        if (definition.isPrimitive){
            return definition.assembly;
        }

        List<String> list = new ArrayList<>();
        for (Definition def : definition.words) {
            list.addAll(expandDefinition(def));
        }
        return list;
    }
    
}
