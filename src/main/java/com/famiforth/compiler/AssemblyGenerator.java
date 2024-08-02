package com.famiforth.compiler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.famiforth.exceptions.GeneratorException;
import com.famiforth.parser.ParserToken;
import com.famiforth.parser.dictionary.UserDictionary;

public class AssemblyGenerator {
    private FileOutputStream fileOutputStream;

    private static List<String> defaultFilesToInclude = Arrays.asList("core.asm", "macros.asm");
    private static String lineSeparator = System.lineSeparator();

    public AssemblyGenerator(FileOutputStream fileOutputStream){
        this.fileOutputStream = fileOutputStream;
    }

    public void writeFileHeader(List<String> headerFileList) throws IOException {
        writeLines(headerFileList.stream().map(str -> String.format(".include \"%s\"", str)).collect(Collectors.toList()));
    }

    public void writeFileHeader() throws IOException {
        writeFileHeader(defaultFilesToInclude);
    }

    public void generate(ParserToken token) throws IOException{
        List<String> lines = null;
        switch(token.type){
            case COLON:
                lines = token.isMacro() ? generateMacro(token) : generateSubroutine(token);
            case IF:
                lines = generateIfStatement(token);
                break;
            case ELSE:
                lines = generateElseStatement(token);
                break;
            case THEN:
                lines = generateThenStatement(token);
                break;
            case INTEGER:
                lines = token.def.getWords();
                break;
            case WORD:
                lines = token.def.getWords();
                break;
            default:
                break;
        }

        writeLines(lines);
    }

    public void close() throws IOException{
        fileOutputStream.close();
    }

    /**
     * Write the list of strings in order
     * seperated by newlines to fileOutputStream
     * @param lines
     * @throws IOException
     */
    protected void writeLines(List<String> lines) throws IOException {
        lines.forEach(line -> {
            try {
                fileOutputStream.write((line + lineSeparator).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                throw new GeneratorException(e.getMessage());
            }
        });
        fileOutputStream.write((lineSeparator).getBytes());
    }

    private List<String> generateMacro(ParserToken token) {
        final String macroHeader = String.format(".macro %s", token.def.getLabel());
        final List<String> words = token.def.getWords();
        final String macroFooter = ".endmacro" + System.lineSeparator();

        List<String> macroList = new LinkedList<>();
        macroList.add(macroHeader);
        macroList.addAll(words.stream().map(UserDictionary::getFlattenedDefinition)
                            .map(lst -> String.format("\t%s", lst.get(0)))
                            .collect(Collectors.toList()));
        macroList.add(macroFooter);
        return macroList;
    }

    private List<String> generateSubroutine(ParserToken token) {
        final String macroHeader = String.format(".proc %s", token.def.getLabel());
        final List<String> words = token.def.getWords();
        final String macroFooter = ".endproc" + System.lineSeparator();

        List<String> subroutineList = new LinkedList<>();
        subroutineList.add(macroHeader);
        subroutineList.addAll(words.stream().map(UserDictionary::getFlattenedDefinition)
                                    .map(lst -> String.format("\t%s", lst.get(0)))
                                    .collect(Collectors.toList()));
        subroutineList.add(macroFooter);
        return subroutineList;
    }

    private List<String> generateIfStatement(ParserToken token) {
        List<String> ifMacro = new ArrayList<>(1);
        ifMacro.add(String.format("%s else_%d", token.def.getLabel(), token.offset));
        return ifMacro;
    }

    private List<String> generateElseStatement(ParserToken token) {
        return List.of("clc", String.format("bcc then_%4d", token.offset), String.format("else_%4d:", token.offset));
    }

    private List<String> generateThenStatement(ParserToken token) {
        return List.of(String.format("then_%4d:", token.offset));
    }
}
