package com.famiforth;

import static org.mockito.Mockito.mock;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.Before;

import com.famiforth.generator.ConfigGenerator;

public class ConfigGeneratorTest {
    
    private ConfigGenerator generator;

    private FileOutputStream fileOut;

    @Before
    public void setup() throws FileNotFoundException{
        fileOut = mock(FileOutputStream.class);
        generator = new ConfigGenerator(fileOut);
    }
}
