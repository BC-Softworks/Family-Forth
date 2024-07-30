package com.famiforth;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Test;


public class CompilerTest {

    String emptySourceFilePath = "src/test/resources/emptyTest.f";
    String testOutputPath = "build/test/out.asm";
    String testDictionaryPath = "src/test/resources/test_dictionary.json";

    Compiler compiler;

    @Test
    public void fileInTest() throws IOException {
        String[] args = { emptySourceFilePath };
        Compiler compiler = new Compiler(args);
        assertEquals(emptySourceFilePath, compiler.fileIn);
    }
    
    @Test
    public void fileOutTest() throws IOException {
        String[] args = {"-o", testOutputPath, emptySourceFilePath};
        Compiler compiler = new Compiler(args);
        assertEquals(testOutputPath, compiler.fileOut);
    }

    @Test
    public void customDictionaryTest() throws IOException {
        String[] args = {"-d", testDictionaryPath, emptySourceFilePath};
        Compiler compiler = new Compiler(args);
        assertEquals(testDictionaryPath, compiler.customDictionary);
    }
    
}

