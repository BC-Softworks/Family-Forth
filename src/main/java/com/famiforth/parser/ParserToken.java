package com.famiforth.parser;

import com.famiforth.parser.dictionary.Definition;

public class ParserToken {

    public final Definition def;
    public final DefinitionType type;
    public final Integer reference;

    public ParserToken(Definition def, DefinitionType type, Integer reference) {
        this.def = def;
        this.type = type;
        this.reference = reference;
    }

    public ParserToken(Definition def, DefinitionType type) {
        this(def, type, null);
    }

    public boolean isMacro(){
        return this.def.isMacro();
    }

    public enum DefinitionType{
        COLON,
        IF,
        ELSE,
        THEN,
        DO,
        LOOP,
        INTEGER,
        WORD
    }
}
