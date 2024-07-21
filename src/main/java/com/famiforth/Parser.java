package com.famiforth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.famiforth.Lexer.Keyword;

public class Parser {

    private Lexer lexer;
    private UserDictionary dictionary;
    
    public Parser(Lexer scan) {
        this.lexer = scan;
        this.dictionary = new UserDictionary();
    }

    public void parse() throws IOException{
        while(lexer.hasNext()){
            List<String> procudureList = new ArrayList<>();

            Pair<String, Lexer.TokenType> token = lexer.next_token();
            switch(token.getRight()){
                case KEYWORD:
                    procudureList = parseKeyword(Keyword.valueOf(token.getLeft()));
                    break;
                case WORD:
                    Definition word = dictionary.getDefinition(token.getLeft());
                    procudureList = expandDefinition(word);
                    break;
                case INTEGER:
                    // TODO: Add proc to push to top
                    procudureList = List.of(token.getLeft());
                    break;
            }

            System.out.println(StringUtils.join(procudureList, '\n'));

        }
    }

    public List<String> parseKeyword(Keyword keyword){
        return List.of("");
    }

    public List<String> expandDefinition(Definition definition){
        if (definition.isPrimitive){
            return List.of(definition.procedure);
        }

        List<String> list = new ArrayList<>();
        for (Definition def : definition.words) {
            list.addAll(expandDefinition(def));
        }
        return list;
    }
    
}
