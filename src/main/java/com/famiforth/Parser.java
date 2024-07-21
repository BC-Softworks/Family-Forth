package com.famiforth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.famiforth.Lexer.Keyword;

public class Parser {

    private Lexer lexer;
    private UserDictionary dictionary;
    
    public Parser(Lexer scan, String dictionaryFile) {
        this.lexer = scan;
        this.dictionary = UserDictionary.getInstance(dictionaryFile);
    }

    public void parse() throws IOException{
        while(lexer.hasNext()){
            List<String> procudureList = new ArrayList<>();

            Lexer.Token token = lexer.next_token();
            switch(token.type){
                case KEYWORD:
                    procudureList = parseKeyword(Keyword.valueOf(token.value));
                    break;
                case WORD:
                    Definition word = dictionary.getDefinition(token.value);
                    procudureList = expandDefinition(word);
                    break;
                case INTEGER:
                    // TODO: Add proc to push to top
                    procudureList = List.of(token.value);
                    break;
            }

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
                throw new IllegalArgumentException(String.format("Syntax Error: Keyword %0 encountered out of order.", keyword.value));
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
