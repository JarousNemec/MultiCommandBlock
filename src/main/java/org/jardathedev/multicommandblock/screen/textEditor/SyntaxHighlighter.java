package org.jardathedev.multicommandblock.screen.textEditor;

import static org.jardathedev.multicommandblock.util.CommandUtil.*;

public class SyntaxHighlighter {
    public static final int COLOR_DEFAULT = 0xFFFFFFFF;
    public static final int COLOR_STRING = 0xFFFFAA33;
    public static final int COLOR_COMMAND = 0xFF55FF55;
    public static final int COLOR_SELECTORS = 0xFFCC66FF;
    public static final int COLOR_NUMBERS = 0xFFFFCC33;
    public static final int COLOR_NTB = 0xFF55FFFF;


    public static int getColor(String line, int index) {

        char c = line.charAt(index);

        // === STRING ===
        if (isQuote(line, index)) {
            return COLOR_DEFAULT;
        }

        if (isInsideString(line, index)) {
            return COLOR_STRING; // orange
        }

        // === COMMAND ===
        if (index == 0 && (c == '/' || c == '%' || c == '#')) {
            return COLOR_COMMAND; // green
        }

        // === SELECTORS ===
        if (c == '@' && index + 1 < line.length()) {
            return COLOR_SELECTORS; // purple
        }

        // === NUMBERS ===
        if (Character.isDigit(c)) {
            return COLOR_NUMBERS; // gold
        }

        String word = getWordAt(line, index);

        // === COMMAND KEYWORDS ===
        if (isKeyword(word, MINECRAFT_COMMAND_KEYWORDS) || isKeyword(word, CUSTOM_COMMAND_KEYWORDS)) {
            return COLOR_COMMAND;
        }

        // === NBT ATTRIBUTES ===
        if (isKeyword(word, NBT_KEYWORDS)) {
            return COLOR_NTB;
        }

        // === NBT KEYS ===
        if (isNBTKey(line, index)) {
            return COLOR_NTB; // cyan
        }

        return COLOR_DEFAULT; // default
    }

    // ─────────────────────────────


}

