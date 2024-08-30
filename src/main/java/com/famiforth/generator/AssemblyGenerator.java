package com.famiforth.generator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.famiforth.exceptions.GeneratorException;
import com.famiforth.parser.ParserToken;
import com.famiforth.parser.dictionary.DefinitionUtils;
import com.famiforth.parser.dictionary.UserDictionary;

/** FamilyForth Code Generator
 * @author Edward Conn
*/
public class AssemblyGenerator extends AbstractGenerator {

    private static final String JSR = "jsr ";
    private static final String JMP = "jmp ";
    private static final String RETURN = "rts";
    
    public AssemblyGenerator(FileOutputStream fileOutputStream) {
        super(fileOutputStream);
    }

    public void writeGuard(String fileName) {
        List<String> lines = new LinkedList<>();
        lines.add(String.format(".ifndef %s_GUARD", fileName));
        lines.add(String.format("\t%s_GUARD = 1", fileName));
        lines.add(".endif");
        writeLines(lines);
    }

    @Override
    public List<String> generate(ParserToken token) throws IOException{
        List<String> lines = new LinkedList<>();
        switch(token.type){
            case COLON:
                lines = token.isMacro() ? generateMacro(token, false) : generateSubroutine(token, false);
                break;
            case CONST:
                lines = token.def.getWords();
                break;
            case CODE:
                lines = generateSubroutine(token, true);
                break;
            case MACRO:
                lines = generateMacro(token, true);
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
            case INCLUDE:
                lines = generateIncludeStatement(token);
                break;
            case REQUIRE:
                lines = generateRequireStatement(token);
                break;
            case SEGMENT:
                lines = generateSegmentStatement(token);
                break;
            // Control word macros that do not need a label / take arguments
            case DO:
            case BEGIN:
            case REPEAT:
                lines = List.of(token.def.getLabel());
                break;
            case INTEGER:
                lines = generatePushStatement(token);
                break;
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
    protected void writeLines(List<String> lines) {
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

    private List<String> generateMacro(ParserToken token, boolean isInline) {
        final String macroHeader = String.format(".macro %s", token.def.getLabel());
        final List<String> words = token.def.getWords();
        final String macroFooter = ".endmacro";

        return generateIndentedCodeBlock(macroHeader, macroFooter, words, isInline);
    }

    private List<String> generateSubroutine(ParserToken token, boolean isInline) {
        final String procHeader = String.format(".proc %s", token.def.getLabel());
        final List<String> words = token.def.getWords();
        final String procFooter = ".endproc";

        return generateIndentedCodeBlock(procHeader, procFooter, words, isInline);
    }

    /**
     * Converts a word list into an indented block of assembly
     * @param header
     * @param footer
     * @param body
     * @param isInline
     */
    private List<String> generateIndentedCodeBlock(String header, String footer, List<String> body, boolean isInline){
        List<String> codeBlock = new LinkedList<>();
        codeBlock.add(header);
        if(isInline){
            codeBlock.addAll(body.stream()
                .map(str -> {
                    if(str.startsWith(JSR) || str.startsWith(JMP)){
                        String[] instruction = str.split(" ");
                        if(instruction.length == 2){
                            if(!UserDictionary.isDefined(instruction[1])){
                                return str;
                            }
                            return instruction[0] + " " + DefinitionUtils.convertToVaildLabel(instruction[1]);
                        } else {
                            throw new GeneratorException("Invalid jump instruction error.");
                        }
                    }
                    return str;
                })
                .map(str -> String.format("\t%s", str))
                .collect(Collectors.toList()));
        } else {
            codeBlock.addAll(body.stream()
                .map(UserDictionary::getDefinition)
                .map(def -> {
                    if(def.isNumber()){
                        String[] splitString = def.getLabel().split(",");
                        StringJoiner joiner = new StringJoiner(lineSeparator);
                        joiner.add("\tPUT");
                        joiner.add(String.format("\tlda #$%s", splitString[0]));
                        joiner.add(String.format("\tsta %s", "00,X"));
                        joiner.add(String.format("\tlda #$%s", splitString[1]));
                        joiner.add(String.format("\tsta %s", "01,X"));
                        return joiner.toString();
                    }
                    String str = def.isMacro() || RETURN.equalsIgnoreCase(def.getLabel()) ? "" : JSR;
                    return String.format("\t%s%s", str, def.getLabel());
                })
                .collect(Collectors.toList()));
            // If the last line is a subroutine use jmp instead of rts
            if(codeBlock.get(codeBlock.size() - 1).startsWith("\t" + JSR)){
                String str = codeBlock.remove(codeBlock.size() - 1);
                codeBlock.add(str.replace(JSR, JMP));
            } else {
                codeBlock.add("\t" + RETURN);
            }
        }
        codeBlock.add(footer);
        return codeBlock;
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

    private List<String> generateRequireStatement(ParserToken token) {
        return List.of(String.format(".include \"%s\"", token.reference.getLeft().substring(0, token.reference.getLeft().indexOf(".")) + ".asm"));
    }

    private List<String> generateIncludeStatement(ParserToken token) {
        return List.of(String.format(".include \"%s\"", token.reference.getLeft().substring(0, token.reference.getLeft().indexOf(".")) + ".asm"));
    }

    private List<String> generateSegmentStatement(ParserToken token) {
        return List.of(String.format(".segment \"%s\"", token.reference.getLeft()));
    }

    private List<String> generatePushStatement(ParserToken token) {
        String[] splitString = token.def.getLabel().split(",");
        List<String> lst = new LinkedList<>();
        lst.add(String.format("lda %s", splitString[0]));
        lst.add(String.format("sta %s", "00,X"));
        lst.add(String.format("lda %s", splitString[1]));
        lst.add(String.format("sta %s", "01,X"));
        return lst;
    }
}
