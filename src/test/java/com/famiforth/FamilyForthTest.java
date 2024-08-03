package com.famiforth;

import java.io.IOException;

import org.junit.Test;

public class FamilyForthTest {
    FamilyForth familyForth;

    String testFileIn = "src/test/resources/test.f";
    String testFileOut = "build/test_out.asm";
    String testDictioanry = "src/test/resources/test_dictionary.json";

    @Test
    public void cmdLineTest() throws IOException {
        String[] args = { testFileIn };
        FamilyForth.main(args);
    }
}
