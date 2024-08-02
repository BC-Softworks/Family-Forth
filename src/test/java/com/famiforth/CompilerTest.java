package com.famiforth;

import java.io.IOException;

import org.junit.Test;

import com.famiforth.compiler.Compiler;

public class CompilerTest {

    Compiler compiler;

    String testFileIn = "src/test/resources/test.f";
    String testFileOut = "build/test_out.asm";
    String testDictioanry = "src/test/resources/test_dictionary.json";

    @Test
    public void compilerTest() throws IOException {
        Compiler compiler = new Compiler(testFileIn, testFileOut, testDictioanry);
        compiler.compile();
    }

}
