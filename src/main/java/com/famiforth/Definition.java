package com.famiforth;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Definition {
    
    private boolean isPrimitive;
    private String name;
    private List<Definition> words;
    private List<String> assembly;


    private Definition() {}

    public static Definition createPrimitiveDefinition(String name, List<String> assembly){
        Definition definition = new Definition();
        definition.isPrimitive = false;
        definition.name = name;
        definition.words = null;
        definition.assembly = assembly;
        
        return definition;
    }

    public static Definition createUserWordDefinition(String name, List<Definition> words){
        Definition definition = new Definition();
        definition.isPrimitive = false;
        definition.name = name;
        definition.words = words;
        definition.assembly = null;
        
        return definition;
    }


    @SuppressWarnings("unchecked")
    public static List<Definition> fromJSONList(List<Object> list){
        if(list.isEmpty()){
            return List.of();
        }

        return list.stream().map(obj -> (HashMap<String, Object>) obj)
                            .map(Definition::fromHashMap)
                            .collect(Collectors.toList());
    }

    
    /**
     * Convert a HashMap of values to a Definition
     * @param obj
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Definition fromHashMap(HashMap<String, Object> map){

        Definition definition = new Definition();
        definition.isPrimitive = Boolean.parseBoolean(map.get("isPrimitive").toString());
        definition.name = map.get("name").toString();
        definition.words = fromJSONList((List<Object>) map.get("words"));
        definition.assembly = List.of(map.get("assembly").toString().split(","));
        return definition;
    }

    @Override
    public String toString() {
        return String.format("Definition [%s: %s]", name, words);
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

    public List<String> getAssembly() {
        return List.copyOf(assembly);
    }
}
