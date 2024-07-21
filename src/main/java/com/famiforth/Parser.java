package com.famiforth;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

public class Parser {

    private Lexer lexer;

    public Parser(Lexer scan) {
        this.lexer = scan;
    }

    public void parse() throws IOException{
        while(lexer.hasNext()){
            Pair<String, Integer> token = lexer.next_token();

            if(Definition.isKeyword(token.getLeft())){
                switch(Definition.Keyword.valueOf(token.getLeft())){
                    case COLON:
                        // parse user defined word
                        break;
                    case SEMICOLON:
                        break;
                    case END:
                        break;
                    case IF:
                        // parse If statement
                        break;
                    case ELSE:
                        break;
                    case THEN:
                        break;
                    case BEGIN:
                        // parse loop word
                        break;
                    case WHILE:
                        break;
                    case REPEAT:
                        break;
                    case UNTIL:
                        break;
                    case DO:
                        // parse loop word
                        break;
                    case LOOP:
                        break;
                    case PLUS_LOOP:
                        break;
                    default:
                        throw new IllegalArgumentException("Syntax Error");
                }
            } else if(Lexer.isInteger(token.getLeft(), 10)){
                int number = Integer.parseInt(token.getLeft());
                // TODO: Add number to stack
            } else {
                
            }
        }
    }
    
}
