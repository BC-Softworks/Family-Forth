package com.famiforth.parser;

import org.apache.commons.lang3.tuple.Pair;

import com.famiforth.parser.dictionary.Definition;

public class ParserToken {

    public final Definition def;
    public final DefinitionType type;
    public final Pair<String, String> reference;
    public final String name;
    public final boolean interrupt;


    public ParserToken(Definition def, DefinitionType type, Pair<String, String> reference, String name) {
        this.def = def;
        this.type = type;
        this.reference = reference;
        this.name = name;
        this.interrupt = false;
    }

    public ParserToken(Definition def, DefinitionType type, Pair<String, String> reference, boolean interrupt) {
        this.def = def;
        this.type = type;
        this.reference = reference;
        this.name = null;
        this.interrupt = interrupt;
    }

    public ParserToken(Definition def, DefinitionType type) {
        this(def, type, null, null);
    }

    public boolean isMacro(){
        return this.def.isMacro();
    }

    public enum DefinitionType{
        COLON,
        IMMEDIATE,
        POSTPONE,
        CONSTANT,
        VARIABLE,
        CODE,
        MACRO,
        CONST,
        CREATE,
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
        INCLUDE,
        REQUIRE,
        SEGMENT,
        INTEGER,
        WORD
    }
}
