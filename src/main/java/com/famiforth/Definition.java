package com.famiforth;

import java.util.List;

public class Definition {
    
    protected boolean isPrimitive;
    
    protected String name;
    
    protected List<Definition> words;

    protected List<String> assembly;


    private Definition() {}
    
    public Definition createPrimitiveDefinition(String name, List<String> assembly){
        Definition definition = new Definition();
        definition.isPrimitive = false;
        definition.name = name;
        definition.words = null;
        definition.assembly = assembly;
        
        return definition;
    }

    public Definition createUserWordDefinition(String name, List<Definition> words){
        Definition definition = new Definition();
        definition.isPrimitive = false;
        definition.name = name;
        definition.words = words;
        definition.assembly = null;
        
        return definition;
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
        return List.copyOf(words);
    }

    public List<String> getProcedure() {
        return List.copyOf(assembly);
    }
}
