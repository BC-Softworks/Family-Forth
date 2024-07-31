package com.famiforth.compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/** FamilyForth cross compiler
 * @author Edward Conn
*/
public class Compiler {

    private Parser parser;
    private FileOutputStream fileOutputStream;

    final static List<String> defaultFilesToInclude = Arrays.asList("core.asm");
    final static String lineSeparator = System.lineSeparator();

    public Compiler(String fileIn, String fileOut, String customDictionary) {

        // Create a Lexer and Parser using the parsed options
        try {
            parser = new Parser(new Lexer(new FileReader(new File(fileIn))), customDictionary);
            fileOutputStream = new FileOutputStream(new File(fileOut));
        } catch (FileNotFoundException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }


    public void parseFile() throws IOException {
        // Write assembly header block first
        writeFileHeader(defaultFilesToInclude);

        // Used for debugging
        List<List<String>> parsedWords = new Stack<>();

        while(parser.hasNext()){
            List<String> parsedWord = parser.parse();
            if(!(parsedWord == null || parsedWord.isEmpty())){
                parsedWords.add(parsedWord);
                fileOutputStream.write((StringUtils.join(parsedWord, lineSeparator) + lineSeparator).getBytes());
            }
        }
        // Close FileOutputStream
        fileOutputStream.close();
        System.out.println("Compilation successful.");
    }

    private void writeFileHeader(List<String> headerFileList) throws IOException {
        String includeBlock = headerFileList.stream()
                                            .map(str -> String.format(".include \"%s\"", str))
                                            .collect(Collectors.joining(lineSeparator));
        fileOutputStream.write((includeBlock + lineSeparator).getBytes());
    }

}
