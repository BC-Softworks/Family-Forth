package com.famiforth.compiler;

import java.io.File;

public class CompilerBuilder {
    private boolean guard = true;
    private boolean header = true;
    private boolean vector = true;
    private boolean oam = true;
    private byte mirror = 0;    // Horizontal
    private byte mapper = 0;    // NROM
    private byte backup = 0;
    private byte prgBanks = 1;
    private byte charBanks = 1;
    private File fileIn;
    private File fileOut;
    private File cfgFile;
    private String initalDictionary;

    public Compiler toCompiler(){
        return new Compiler(guard, header, vector, oam, mirror, mapper, backup, prgBanks, charBanks, fileIn, fileOut, cfgFile, initalDictionary);
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public void setVector(boolean vector) {
        this.vector = vector;
    }

    public void setOAM(boolean oam) {
        this.oam = oam;
    }

    public void setMirror(byte mirror) {
        this.mirror = mirror;
    }

    public void setMapper(byte mapper) {
        this.mapper = mapper;
    }

    public void setBackup(byte backup) {
        this.backup = backup;
    }

    public void setPrgBanks(byte prgBanks) {
        this.prgBanks = prgBanks;
    }

    public void setCharBanks(byte charBanks) {
        this.charBanks = charBanks;
    }

    public void setFileIn(File fileIn) {
        this.fileIn = fileIn;
    }

    public void setFileOut(File fileOut) {
        this.fileOut = fileOut;
    }

    public void setCfgFile(File cfgFile) {
        this.cfgFile = cfgFile;
    }

    public void setInitalDictionary(String initalDictionary) {
        this.initalDictionary = initalDictionary;
    }

}
