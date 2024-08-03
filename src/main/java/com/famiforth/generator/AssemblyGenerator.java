package com.famiforth.generator;

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

public class AssemblyGenerator extends AbstractGenerator {

    private static List<String> defaultFilesToInclude = Arrays.asList("core.asm", "macros.asm");
    
    public AssemblyGenerator(FileOutputStream fileOutputStream) {
        super(fileOutputStream);
    }

    @Override
    public void writeFileHeader() throws IOException {
        writeFileHeader(defaultFilesToInclude);
    }

    private void writeFileHeader(List<String> headerFileList) throws IOException {
        writeLines(headerFileList.stream().map(str -> String.format(".include \"%s\"", str)).collect(Collectors.toList()));
    }

    @Override
    public void generate(ParserToken token) throws IOException{
        List<String> lines = new ArrayList<>();
        switch(token.type){
            case COLON:
                lines = token.isMacro() ? generateMacro(token) : generateSubroutine(token);
                break;
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
                lines = List.of(token.def.getLabel());
            case WORD:
                lines = List.of((token.def.isMacro() ? "" : "jsr") + token.def.getLabel());
                break;
            default:
                break;
        }

        writeLines(lines);
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
                getFileOutputStream().write((line).getBytes());
                getFileOutputStream().write((lineSeparator).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                throw new GeneratorException(e.getMessage());
            }
        });
    }

    private List<String> generateMacro(ParserToken token) {
        final String macroHeader = String.format(".macro %s", token.def.getLabel());
        final List<String> words = token.def.getWords();
        final String macroFooter = ".endmacro";

        List<String> macroList = new LinkedList<>();
        macroList.add(macroHeader);
        macroList.addAll(words.stream().map(UserDictionary::getFlattenedDefinition)
                            .map(lst -> String.format("\t%s", lst.get(0)))
                            .collect(Collectors.toList()));
        macroList.add(macroFooter);
        return macroList;
    }

    private List<String> generateSubroutine(ParserToken token) {
        final String procHeader = String.format(".proc %s", token.def.getLabel());
        final List<String> words = token.def.getWords();
        final String procFooter = ".endproc";

        List<String> subroutineList = new LinkedList<>();
        subroutineList.add(procHeader);
        subroutineList.addAll(words.stream().map(UserDictionary::getFlattenedDefinition)
                                    .map(lst -> String.format("\t%s", lst.get(0)))
                                    .collect(Collectors.toList()));
        subroutineList.add(procFooter);
        return subroutineList;
    }

    private List<String> generateIfStatement(ParserToken token) {
        return List.of(String.format("%s else_%d", token.def.getLabel(), token.condOffset));
    }

    private List<String> generateElseStatement(ParserToken token) {
        return List.of("clc", String.format("bcc then_%d", token.condOffset), String.format("else_%d:", token.condOffset));
    }

    private List<String> generateThenStatement(ParserToken token) {
        return List.of(String.format("then_%d:", token.condOffset));
    }
}
