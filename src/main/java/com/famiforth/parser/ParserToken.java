package com.famiforth.parser;

import com.famiforth.parser.dictionary.Definition;

public class ParserToken {

    public final Definition def;
    public final DefinitionType type;
    public final int offset;

    public ParserToken(Definition def, DefinitionType type, int offset) {
        this.def = def;
        this.type = type;
        this.offset = offset;
    }

    public boolean isMacro(){
        return this.def.isMacro();
    }

    public enum DefinitionType{
        COLON,
        IF,
        ELSE,
        THEN,
        INTEGER,
        WORD
    }
}
