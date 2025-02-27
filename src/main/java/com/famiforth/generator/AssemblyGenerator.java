package com.famiforth.generator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.famiforth.exceptions.GeneratorException;
import com.famiforth.parser.ParserToken;
import com.famiforth.parser.dictionary.DefinitionUtils;
import com.famiforth.parser.dictionary.UserDictionary;

/** FamilyForth Code Generator
 * @author Edward Conn
*/
public class AssemblyGenerator extends Generator{

    private static final String JSR = "jsr ";
    private static final String JMP = "jmp ";
    private static final String RET = "rts";
    private static final String RTI = "rti";

    
    public AssemblyGenerator(FileOutputStream fileOutputStream) {
        super(fileOutputStream);
    }

    /**
     * Generate config
     * @param fileName
     */
    public void writeConfigurationFile(String fileName){
        
    }

    /**
     * Initalize Forth registers
     * @param fileName
     */
    public void writeRegisters(String fileName){
        
    }

    /**
     * Add a iNES header to the beginning of the main assembly file
     * @param fileName
     * @param mapper
     * @param mirror True if mirrored vertically, false mirrored if horizontally
     * @param backup True if the cartridge has SRAM
     * @param prgBanks Number of 16k PRG Banks
     * @param charBanks Number of 8k CHR Banks
     */
    public void writeHeader(String fileName, byte mapper, byte mirror, byte backup, byte prgBanks, byte charBanks) {
        List<String> lines = new LinkedList<>();
        lines.add(".segment \"HEADER\"");
        lines.add(String.format("INES_MAPPER = $%2s", mapper > 10 ? mapper : "0" + mapper));
        lines.add(String.format("INES_MIRROR = $%2s", mirror > 10 ? mirror : "0" + mirror));
        lines.add(String.format("INES_SRAM   = $%2s", backup > 10 ? backup : "0" + backup));
        
        lines.add(".byte 'N', 'E', 'S', $1A");
        lines.add(String.format(".byte $%2s", prgBanks > 10 ? prgBanks : "0" + prgBanks));   // Number of PRG banks
        lines.add(String.format(".byte $%2s", charBanks > 10 ? charBanks : "0" + charBanks));  // Number of CHR banks
        lines.add(".byte INES_MIRROR | (INES_SRAM << 1) | ((INES_MAPPER & $f) << 4)");
        lines.add(".byte (INES_MAPPER & %11110000)");
        lines.add(".byte $0, $0, $0, $0, $0, $0, $0, $0");
        lines.add("");
        writeLines(lines);
    }

    /**
     * Write a file include guard
     * @param fileName
     */
    public void writeGuard(String fileName) {
        List<String> lines = new LinkedList<>();
        lines.add(String.format(".ifndef %s_GUARD", fileName));
        lines.add(String.format("\t%s_GUARD = 1", fileName));
        lines.add(".endif");
        writeLines(lines);
    }

    /**
     * Write nmi, irq, and reset vectors
     * @param fileName
     */
    public void writeVector(String fileName) {
        List<String> lines = new LinkedList<>();
        lines.add(".segment \"VECTORS\"");
        lines.add(".word nmi");
        lines.add(".word reset");
        lines.add(".word irq");
        lines.add("");
        writeLines(lines);
    }

    /**
     * Reserve OAM
     * @param fileName
     */
    public void writeOAM(String fileName) {
        List<String> lines = new LinkedList<>();
        lines.add(".segment \"OAM\"");
        lines.add("oam: .res 256");
        lines.add("");
        writeLines(lines);
    }

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
            case CONSTANT:
            case MACRO:
            case VARIABLE:
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
            case WORD:
                lines = List.of((token.def.isMacro() ? "" : JSR) + token.def.getLabel());
                break;
            default:
                break;
        }

        writeLines(lines);

        return lines;
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
                        if(instruction.length >= 2){
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
                    String str = def.isMacro() || RET.equalsIgnoreCase(def.getLabel()) ? "" : JSR;
                    return String.format("\t%s%s", str, def.getLabel());
                })
                .collect(Collectors.toList()));
            String lastLine = codeBlock.get(codeBlock.size() - 1);
            if(lastLine.startsWith("\t" + JSR)){
                // If the last line is a subroutine use jmp instead of rts
                String str = codeBlock.remove(codeBlock.size() - 1);
                codeBlock.add(str.replace(JSR, JMP));
            } else if(!(lastLine.startsWith("\t" + RTI) && lastLine.startsWith("\t" + RET))){
                // Add a return if an explict one was not used
                codeBlock.add("\t" + RET);
            }
        }
        codeBlock.add(footer);
        return codeBlock;
    }

    private List<String> generateIfStatement(ParserToken token) {
        List<String> lst = new LinkedList<>();
        lst.add(String.format("PUSHORIG %s", token.reference.getLeft()));
        lst.add(token.def.getLabel());
        return List.of(); 
    }

    private List<String> generateElseStatement(ParserToken token) {
        List<String> lst = new LinkedList<>();
        lst.add(String.format("%s: ", token.reference.getLeft()));
        lst.add(String.format("%s", token.def.getLabel()));
        lst.add(String.format("PUSHORIG %s", token.reference.getRight()));
        return lst;
    }

    private List<String> generateThenStatement(ParserToken token) {
        List<String> lst = new LinkedList<>();
        lst.add(String.format("%s", token.def.getLabel()));
        lst.add(String.format("%s: ", token.reference.getLeft()));
        return lst;
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
}
