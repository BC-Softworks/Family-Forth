package com.famiforth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Definition {

    private boolean isPrimitive;
    private String name;
    private List<String> words;
    private List<String> assembly;

    private Definition() {
    }

    public static Definition createPrimitiveDefinition(String name, List<String> assembly) {
        Definition definition = new Definition();
        definition.isPrimitive = true;
        definition.name = name;
        definition.words = List.of();
        definition.assembly = assembly;

        return definition;
    }

    public static Definition createUserWordDefinition(String name, List<String> words) {
        Definition definition = new Definition();
        definition.isPrimitive = false;
        definition.name = name;
        definition.words = words;
        definition.assembly = List.of();

        return definition;
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
     * 
     * @param obj
     * @return
     */
    public static Definition fromHashMap(HashMap<String, Object> map) {

        Definition definition = new Definition();
        definition.isPrimitive = Boolean.parseBoolean(map.get("isPrimitive").toString());
        definition.name = map.get("name").toString();
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

    public List<String> getWords() {
        assert words != null;
        return List.copyOf(words);
    }

    public List<String> getAssembly() {
        assert assembly != null;
        return List.copyOf(assembly);
    }

    /**
     * Converts the provided Definition to a List of Strings
     * @param definition
     * @return Assembly definition of word
     */
    public List<String> expandDefinition() {
        if (isPrimitive()) {
            return getAssembly();
        }

        return getWords().stream().map(UserDictionary::getDefinition)
                            .map(Definition::expandDefinition)
                            .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
    }
}
