package com.famiforth.parser;

import com.famiforth.parser.dictionary.Definition;

public class ParserToken {

    public final Definition def;
    public final DefinitionType type;
    public final int condOffset;
    public final int loopOffset;


    public ParserToken(Definition def, DefinitionType type, int condOffset, int loopOffset) {
        this.def = def;
        this.type = type;
        this.condOffset = condOffset;
        this.loopOffset = loopOffset;
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
