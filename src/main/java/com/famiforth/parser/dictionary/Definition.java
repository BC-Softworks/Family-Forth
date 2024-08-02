package com.famiforth.parser.dictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/** Represents a word in the UserDictionary
 * @author Edward Conn
 */
public class Definition {

    /**
     * Create a low level word defined as a sequence of assembly code
     * @param name
     * @param assembly
     * @param isMacro
     */
    protected Definition(String name, String label, boolean isMacro) {
        this.isPrimitive = true;
        this.isMacro = isMacro;
        this.name = name;
        this.label = label;
        this.words = List.of();
    }

    /**
     * Create a higher level word constructed from 
     * already defined subroutines and macros
     * @param name
     * @param words
     * @param isMacro 
     */
    protected Definition(String name, String label, boolean isMacro, List<String> words) {
        this.isPrimitive = false;
        this.isMacro = isMacro;
        this.name = name;
        this.label = label;
        this.words = words;
    }

    protected Definition(String name, boolean isMacro, List<String> words) {
        this.isPrimitive = false;
        this.isMacro = isMacro;
        this.name = name;
        this.label = DefinitionUtils.convertToVaildLabel(name);
        this.words = words;
    }

    private Definition(){}

    @SuppressWarnings("unchecked")
    public static List<Definition> fromJSONList(List<Object> list) {
        if (list.isEmpty()) {
            return List.of();
        }

        return list.stream().map(obj -> (HashMap<String, Object>) obj)
                .map(Definition::fromHashMap)
                .collect(Collectors.toList());
    }

    /**
     * Convert a HashMap of values to a Definition
     * @param obj
     * @return the Definition represented by the provided HashMap
     */
    public static Definition fromHashMap(HashMap<String, Object> map) {

        Definition definition = new Definition();
        definition.isPrimitive = true; // Only primitives maybe included in a json map
        definition.name = map.get("name").toString();
        definition.label = map.get("label").toString();
        definition.isMacro = Boolean.parseBoolean(map.get("isMacro").toString());
        definition.words = List.of();
        return definition;
    }

    private boolean isMacro;

    private boolean isPrimitive;

    private String name;
    
    private String label;

    private List<String> words;

    @Override
    public String toString() {
        return String.format("Definition [%s: %s]", name, words);
    }
    
    public boolean isMacro() {
        return isMacro;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }
    public String getName() {
        return name;
    }
    public String getLabel() {
        return label;
    }
    public List<String> getWords() {
        assert words != null;
        return List.copyOf(words);
    }
    
    /**
     * Flattens a Definition into a list of assembly instructions.
     * @param definition
     * @return a subroutine where all user defined words are inlined to the given depth
     */
    protected List<String> flattenDefinition() {
        if (isPrimitive()) {
            return List.of(isMacro ? getLabel() : UserDictionary.getSubroutine(getName()));
        }

        return getWords().stream().map(UserDictionary::getDefinition)
                            .map(Definition::flattenDefinition)
                            .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
    }
}
