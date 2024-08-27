package com.famiforth;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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

    static File fileIn;
    static File fileOut;
    static Path pathOut;
    static String customDictionary;
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
                pathOut = Path.of(cmd.getOptionValue("o"));
            }

            List<String> unparsedArgs = cmd.getArgList();
            if(unparsedArgs.isEmpty()){
                System.err.println("Error: No input file provided");
                System.exit(0);
            }

            fileIn = new File(unparsedArgs.get(0));
            if(!fileIn.canRead()){
                System.err.println("Error: Unable to read source file.");
                System.exit(0);
            }

            
            String fileOutName = fileIn.getName().substring(0, fileIn.getName().lastIndexOf(".")) + ".asm";
            fileOut = pathOut == null ? new File(fileOutName) : new File(pathOut.toString() + File.separator + fileOutName);
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
