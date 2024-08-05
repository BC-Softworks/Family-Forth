package com.famiforth;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.famiforth.compiler.Compiler;


public class FamilyForth {
    static final String DEFAULT_FILE_OUT = "build/out.asm";
    static final String DEFAULT_DICTIONARY_LOCATION = "src/main/resources/json/core_dictionary.json";

    static String fileIn;
    static String fileOut = DEFAULT_FILE_OUT;
    static String customDictionary = DEFAULT_DICTIONARY_LOCATION;
    static String[] includedFiles;

    private static void parseArguments(String args[]){
        CommandLineParser cmdLine = new DefaultParser();
        Options options = createOptions();

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

            if(cmd.hasOption("i")){
                includedFiles = cmd.getOptionValues("i");
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

        } catch (ParseException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }
    
    private static Options createOptions() {
        Option dic = new Option("d", "dictionary", true, "Custom dictionary");
        Option help = new Option("h", "help", false, "print this message");
        Option include = new Option("i", "include", true, "assembly files to include");
        Option out = new Option("o", "output", true, "Place the output into <file>");
        return new Options().addOption(dic)
                            .addOption(help)
                            .addOption(include)
                            .addOption(out);
    }

    public static void main(String args[]) throws IOException {
        parseArguments(args);
        try {
            Compiler compiler = new Compiler(fileIn, fileOut, customDictionary);
            compiler.compile();
        } catch (Throwable e) {
            System.err.println("\t" + e.getMessage());
            System.exit(1);
        }
    }
}
