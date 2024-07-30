package com.famiforth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** FamilyForth cross compiler
 * @author Edward Conn
*/
public class Compiler {

    final String DEFAULT_FILE_OUT = "build/out.asm";
    final String DEFAULT_DICTIONARY_LOCATION = "src/main/resources/base_dictionary.json";

    final Lexer scan;
    final Parser parser;

    String fileIn;
    String fileOut = DEFAULT_FILE_OUT;
    String customDictionary = DEFAULT_DICTIONARY_LOCATION;

    public Compiler(String[] args) {
        CommandLineParser cmdLine = new DefaultParser();
        Options options = createOptions();
        Reader reader = null;

        try {
            CommandLine cmd = cmdLine.parse(options, args);

            if(cmd.hasOption("h")){
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("familyforth [OPTION]... FILENAME", options);
                System.exit(0);
            }

            if(cmd.hasOption("d")){
                customDictionary = cmd.getOptionValue("d");
                if(!new File(customDictionary).canWrite()){
                    System.err.println("Error: Unable to open provided dictionary.");
                    System.exit(0);
                }
            }

            if(cmd.hasOption("o")){
                fileOut = cmd.getOptionValue("o");
                if(!new File(fileOut).canWrite()){
                    System.err.println("Error: Unable to write to output file.");
                    System.exit(0);
                }
            }

            List<String> unparsedArgs = cmd.getArgList();
            if(unparsedArgs.isEmpty()){
                System.err.println("Error: No input file provided");
                System.exit(0);
            }

            fileIn = unparsedArgs.get(0);
            if(!new File(fileIn).canRead()){
                System.err.println("Error: Unable to read source file.");
                System.exit(0);
            }

            // Create a FileReader if the provided file is accessible
            reader = new FileReader(new File(fileIn));

        } catch (FileNotFoundException | ParseException ex) {
            System.err.println("Error: " + ex.getMessage());
        }

        // Create a Lexer and Parser using the parsed options
        scan = new Lexer(reader);
        parser = new Parser(scan, customDictionary, fileOut);
    }

    private Options createOptions() {
        Option dic = new Option("d", "dictionary", true, "Custom dictionary");
        Option help = new Option("h", "help", false, "print this message");
        Option out = new Option("o", "output", true, "Place the output into <file>");
        return new Options().addOption(dic)
                            .addOption(help)
                            .addOption(out);
    }

    private void parseFile() throws IOException {
        parser.parse();
    }

    public static void main(String args[]) throws IOException {
        Compiler comp = new Compiler(args);
        try {
            comp.parseFile();
        } catch (Throwable e) {
            System.err.println("     " + e.getMessage());
            System.exit(1);
        }
    }

}
