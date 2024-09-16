package com.famiforth.generator;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ConfigGenerator extends Generator {

    public ConfigGenerator(FileOutputStream fileOutputStream) {
        super(fileOutputStream);
    }
    
    public void writeConfigFile(){
        writeLines(generateMemorySection());
        writeLines(generateSegmentSection());
    }

    protected List<String> generateMemorySection(){
        List<String> lines = new ArrayList<>();
        lines.add("MEMORY {");
        lines.add(memoryLine("ZP", "", "$0002", "$001A", true, false));
        lines.add(memoryLine("HEADER", "%0", "$0000", "$0010", true, false));
        lines.add(memoryLine("ROM", "%0", "$8000", "$7FFA", true, true));
        lines.add(memoryLine("VECTOR", "%0", "$FFFA", "$0006", true, false));
        lines.add(memoryLine("CHAR", "%0", "$0000", "$2000", true, false));
        lines.add(memoryLine("RAM", "", "$6000", "$2000", true, true));
        lines.add("}");
        return lines;
    }

    protected List<String> generateSegmentSection(){
        List<String> lines = new ArrayList<>();
        lines.add("SEGMENTS {");
        lines.add(segmentLine("ZEROPAGE", "ZP", "zp", false));
        lines.add(segmentLine("HEADER", "HEADER", "ro", false));
        lines.add(segmentLine("STARTUP", "ROM", "ro", true));
        lines.add(segmentLine("CODE", "ROM", "ro", true));
        lines.add(segmentLine("RODATA", "ROM", "ro", true));
        lines.add(segmentLine("VECTORS", "VECTOR", "rw", false));
        lines.add(segmentLine("CHARS", "CHAR", "rw", false));
        lines.add("}");
        return lines;
    }

    private String memoryLine(String name, String file, String start, String size, boolean fill, boolean define){
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%s:   ", name));
        builder.append(String.format("file = %s,", file));
        builder.append(String.format("start = %s,", start));
        builder.append(String.format("size = %s,", size));
        builder.append(String.format("fill = %s,", fill ? "yes" : "no"));
        builder.append(String.format("define = %s;", define ? "yes" : "no"));
        return builder.toString();
    }

    private String segmentLine(String name, String load, String type, boolean define){
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%s:   ", name));
        builder.append(String.format("load = %s,", load));
        builder.append(String.format("type = %s,", type));
        builder.append(String.format("define = %s;", define ? "yes" : "no"));
        return builder.toString();
    }
}

