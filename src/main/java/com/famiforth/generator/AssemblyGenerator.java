package com.famiforth.generator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.famiforth.exceptions.GeneratorException;
import com.famiforth.parser.ParserToken;
import com.famiforth.parser.dictionary.UserDictionary;

/** FamilyForth Code Generator
 * @author Edward Conn
*/
public class AssemblyGenerator extends AbstractGenerator {

    private static final String JSR = "jsr ";
    private static List<String> defaultFilesToInclude = Arrays.asList("init.asm");
    
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
    public List<String> generate(ParserToken token) throws IOException{
        List<String> lines = new LinkedList<>();
        switch(token.type){
            case COLON:
                lines = token.isMacro() ? generateMacro(token) : generateSubroutine(token);
                break;
            case CODE:
                lines = token.def.getWords();
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
            case LOOP:
            case PLUSLOOP:
                lines = generateLoopStatement(token);
                break;
            case LEAVE:
                lines = generateLeaveStatement(token);
                break;
            case WHILE:
                lines = generateWhileStatement(token);
                break;
            case RECURSE:
                lines = generateRecurseStatement(token);
                break;
            case INTEGER:
                lines = List.of(token.def.getLabel());
            // Control word macros that do not need a label / take arguments
            case DO:
            case BEGIN:
            case REPEAT:
            case WORD:
                lines = List.of((token.def.isMacro() ? "" : JSR) + token.def.getLabel());
                break;
            default:
                break;
        }

        writeLines(lines);

        return lines;
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
        macroList.addAll(words.stream().map(UserDictionary::getDefinition)
                                        .map(def -> String.format("\t%s%s", def.isMacro() ? "" : JSR, def.getLabel()))
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
        subroutineList.addAll(words.stream()
                                    .map(UserDictionary::getDefinition)
                                    .map(def -> String.format("\t%s%s", def.isMacro() ? "" : JSR, def.getLabel()))
                                    .collect(Collectors.toList()));
        subroutineList.add(procFooter);
        return subroutineList;
    }

    private List<String> generateIfStatement(ParserToken token) {
        return List.of(String.format("%s %s", token.def.getLabel(), token.reference.getLeft()));
    }

    private List<String> generateElseStatement(ParserToken token) {
        List<String> lst = new LinkedList<>();
        lst.add("clc");
        lst.add(String.format("bcc %s", token.reference.getRight()));
        lst.add(String.format("%s: %s", token.reference.getLeft(), token.def.getLabel()));
        return lst;
    }

    private List<String> generateThenStatement(ParserToken token) {
        return List.of(String.format("%s: %s", token.reference.getLeft(), token.def.getLabel()));
    }

    private List<String> generateLoopStatement(ParserToken token) {
        List<String> lst = new LinkedList<>();
        lst.add(String.format("%s", token.def.getLabel()));
        lst.add(String.format("%s", token.reference.getLeft()));
        return lst;
    }

    private List<String> generateLeaveStatement(ParserToken token) {
        return List.of(String.format("%s %s", token.def.getLabel(), token.reference.getLeft()));
    }

    private List<String> generateWhileStatement(ParserToken token) {
        return List.of(String.format("%s %s", token.reference.getLeft(), token.def.getLabel()));
    }

    private List<String> generateRecurseStatement(ParserToken token) {
        return List.of(String.format("%s %s", token.def.getLabel(), token.name));
    }

}
