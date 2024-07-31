package com.famiforth.dictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/** Represents a word in the UserDictionary
 * @author Edward Conn
 */
public class Definition {

    private boolean isMacro;

    private boolean isPrimitive;

    private String name;

    private List<String> words;

    private List<String> assembly;

    private Definition() {}

    @Override
    public String toString() {
        return String.format("Definition [%s: %s]", name, words);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Definition other = (Definition) obj;
        if (isMacro != other.isMacro)
            return false;
        if (isPrimitive != other.isPrimitive)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (words == null) {
            if (other.words != null)
                return false;
        } else if (!words.equals(other.words))
            return false;
        if (assembly == null) {
            if (other.assembly != null)
                return false;
        } else if (!assembly.equals(other.assembly))
            return false;
        return true;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public String getName() {
        return name;
    }

    public List<String> getWords() {
        assert words != null;
        return List.copyOf(words);
    }

    public List<String> getAssembly() {
        assert assembly != null;
        return List.copyOf(assembly);
    }

    /**
     * Create a low level word defined as a sequence of assembly code
     * @param name
     * @param assembly
     * @param isMacro
     */
    public static Definition createPrimitiveDefinition(String name, List<String> assembly, boolean isMacro) {
        Definition definition = new Definition();
        definition.isPrimitive = true;
        definition.isMacro = isMacro;
        definition.name = DefinitionUtils.convertToName(name);
        definition.words = List.of();
        definition.assembly = assembly;

        return definition;
    }
    public static Definition createPrimitiveDefinition(String name, List<String> assembly) {
        return createUserWordDefinition(name, assembly, false);

    }
    
    /**
     * Create a higher level word constructed from 
     * already defined subroutines and macros
     * @param name
     * @param words
     * @param isMacro 
     */
    public static Definition createUserWordDefinition(String name, List<String> words, boolean isMacro) {
        Definition definition = new Definition();
        definition.isPrimitive = false;
        definition.isMacro = isMacro;
        definition.name = DefinitionUtils.convertToName(name);
        definition.words = words;
        // Prepend jsr if isMacro is false
        definition.assembly = isMacro ? List.of(name) : List.of(String.join("jsr", name, " "));

        return definition;
    }
    public static Definition createUserWordDefinition(String name, List<String> words) {
        return createUserWordDefinition(name, words, false);
    }
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
        definition.isPrimitive = Boolean.parseBoolean(map.get("isPrimitive").toString());
        definition.isMacro = Boolean.parseBoolean(map.get("isMacro").toString());
        definition.name = DefinitionUtils.convertToName(map.get("name").toString());
        definition.words = List.of(map.get("words").toString().split(","))
                .stream()
                .map(s -> s.substring(1, s.length() - 1))
                .map(String::trim)
                .collect(Collectors.toList());
        definition.assembly = List.of(map.get("assembly").toString().split(","))
                .stream()
                .map(s -> s.substring(1, s.length() - 1))
                .map(String::trim)
                .collect(Collectors.toList());
        return definition;
    }

    /**
     * Flattens a Definition into a list of assembly instructions.
     * @param definition
     * @return a subroutine where all user defined words are inlined to the given depth
     */
    public List<String> flattenDefinition() {
        if (isPrimitive()) {
            return getAssembly();
        }

        return getWords().stream().map(UserDictionary::getDefinition)
                            .map(Definition::flattenDefinition)
                            .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
    }
}
