package com.famiforth;

import java.util.Arrays;
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


    public static List<Definition> fromJSONList(List<Object> list){
        return list.stream().map(Definition::fromObject).collect(Collectors.toList());
    }
    
    /**
     * Convert a JSONObject to a Definition
     * @param obj
     * @return
     */
    public static Definition fromObject(Object obj){
        @SuppressWarnings("unchecked")
        HashMap<String, Object> objMap =  (HashMap<String, Object>) obj;

        System.out.print(objMap.get("assembly"));
        System.out.print(objMap.get("words"));

        Definition definition = new Definition();
        definition.isPrimitive = Boolean.parseBoolean(objMap.get("isPrimitive").toString());
        definition.name = (String) objMap.get("name");
        //definition.words = List.of(objMap.get("words"));
        //definition.assembly = List.of(objMap.get("assembly"));
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
