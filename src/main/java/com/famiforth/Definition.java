package com.famiforth;

import java.util.List;

public class Definition {
    
    final boolean isPrimitive;
    
    final String name;
    
    final List<Definition> words;

    // Name of subroutine
    final String procedure;


    public Definition(boolean isPrimitive, String name, String procedure) {
        this.isPrimitive = isPrimitive;
        this.name = name;
        this.procedure = procedure;
        this.words = null;
    }

    public Definition(boolean isPrimitive, String name, List<Definition> words) {
        this.isPrimitive = isPrimitive;
        this.name = name;
        this.words = words;
        this.procedure = null;
    }

    @Override
    public String toString() {
        return String.format("Definition [%0: %1]", name, words);
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public String getName() {
        return name;
    }

    public List<Definition> getWords() {
        return words;
    }

    public String getProcedure() {
        return procedure;
    }
}
