package com.famiforth;

import java.io.IOException;

import org.junit.Test;

import com.famiforth.compiler.Compiler;

public class CompilerTest {
    
    private Compiler compiler;

    private String fileIn;
    private String fileOut;


    @Test
    public void compileCoreTest() throws IOException {
        fileIn = "src/main/forth/core.f";
        fileOut = "build/asm/core.asm";
        compiler = new Compiler(fileIn, fileOut, null);
        compiler.compile();
    }
}
