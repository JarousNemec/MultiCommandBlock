package org.jardathedev.multicommandblock.model;


public class CommandLine {

    private final String raw;
    private final LineType type;

    public CommandLine(String raw, LineType type) {
        this.raw = raw;
        this.type = type;
    }

    public String raw() {
        return raw;
    }

    public LineType type() {
        return type;
    }

    public String commandBody() {
        return raw.length() > 1 ? raw.substring(1) : "";
    }
}

