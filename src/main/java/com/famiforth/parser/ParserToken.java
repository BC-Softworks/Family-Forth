package com.famiforth.parser;

import org.apache.commons.lang3.tuple.Pair;

import com.famiforth.parser.dictionary.Definition;

public class ParserToken {

    public final Definition def;
    public final DefinitionType type;
    public final Pair<String, String> reference;
    public final String name;


    public ParserToken(Definition def, DefinitionType type, Pair<String, String> reference, String name) {
        this.def = def;
        this.type = type;
        this.reference = reference;
        this.name = name;
    }

    public ParserToken(Definition def, DefinitionType type, Pair<String, String> reference) {
        this.def = def;
        this.type = type;
        this.reference = reference;
        this.name = null;
    }

    public ParserToken(Definition def, DefinitionType type) {
        this(def, type, null, null);
    }

    public boolean isMacro(){
        return this.def.isMacro();
    }

    public enum DefinitionType{
        COLON,
        CODE,
        IF,
        ELSE,
        THEN,
        DO,
        LOOP,
        PLUSLOOP,
        LEAVE,
        BEGIN,
        WHILE,
        REPEAT,
        RECURSE,
        INTEGER,
        WORD
    }
}
