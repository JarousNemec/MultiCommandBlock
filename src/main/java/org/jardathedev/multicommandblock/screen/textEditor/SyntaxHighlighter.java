package org.jardathedev.multicommandblock.screen.textEditor;

public class SyntaxHighlighter {
    public static final int COLOR_DEFAULT = 0xFFFFFFFF;
    public static final int COLOR_STRING = 0xFFFFAA33;
    public static final int COLOR_COMMAND = 0xFF55FF55;
    public static final int COLOR_SELECTORS = 0xFFCC66FF;
    public static final int COLOR_NUMBERS = 0xFFFFCC33;
    public static final int COLOR_NTB = 0xFF55FFFF;

    private static final String[] COMMAND_KEYWORDS = {
            "execute", "summon", "give", "tp", "effect", "data", "kill"
    };

    private static final String[] NBT_KEYWORDS = {
            "CustomName", "Color", "ActiveEffects", "Duration",
            "Amplifier", "Enchantments", "AttributeModifiers",
            "Name", "Amount", "Operation", "UUID"
    };


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
        if (index == 0 && c == '/') {
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
        if (isKeyword(word, COMMAND_KEYWORDS)) {
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

    private static boolean isPartOfWord(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static String getWordAt(String line, int index) {
        if (!isPartOfWord(line.charAt(index))) return null;

        int start = index;
        int end = index;

        while (start > 0 && isPartOfWord(line.charAt(start - 1))) start--;
        while (end < line.length() - 1 && isPartOfWord(line.charAt(end + 1))) end++;

        return line.substring(start, end + 1);
    }

    private static boolean isKeyword(String word, String[] keywords) {
        if (word == null) return false;
        for (String kw : keywords) {
            if (kw.equals(word)) return true;
        }
        return false;
    }

    private static boolean isQuote(String line, int index) {
        return line.charAt(index) == '"' &&
                (index == 0 || line.charAt(index - 1) != '\\');
    }

    private static boolean isInsideString(String line, int index) {
        boolean inside = false;
        for (int i = 0; i < index; i++) {
            if (line.charAt(i) == '"' && (i == 0 || line.charAt(i - 1) != '\\')) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static boolean isNBTKey(String line, int index) {
        if (!Character.isLetter(line.charAt(index))) return false;

        int i = index;
        while (i < line.length() && Character.isLetter(line.charAt(i))) {
            i++;
        }

        return i < line.length() && line.charAt(i) == ':';
    }
}

