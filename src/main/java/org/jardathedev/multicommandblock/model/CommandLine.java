package org.jardathedev.multicommandblock.model;


public record CommandLine(String raw, LineType type, LineState state, int indentLevel, boolean isExecutable, boolean hasBody) {

    public boolean isValid() {
        return state == LineState.VALID;
    }

    public boolean isBlank() {
        return raw.isBlank();
    }

    public boolean isComment(){
        return type == LineType.COMMENT;
    }

    public boolean isMinecraft(){
        return type == LineType.MINECRAFT;
    }

    public boolean isCustom(){
        return type == LineType.CUSTOM;
    }

    public String commandBody() {
        return raw.length() > 1 ? raw.substring(1) : "";
    }
}

