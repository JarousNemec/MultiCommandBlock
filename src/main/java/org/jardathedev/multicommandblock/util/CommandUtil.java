package org.jardathedev.multicommandblock.util;

import org.jardathedev.multicommandblock.Multicommandblock;
import org.jardathedev.multicommandblock.shared.CommandLine;
import org.jardathedev.multicommandblock.shared.ExecutionFrame;

import java.util.ArrayList;
import java.util.List;

public class CommandUtil {

    public static final String[] MINECRAFT_COMMAND_KEYWORDS = {"help", "say", "tell", "tellraw", "msg", "w", "me", "trigger", "give", "clear", "kill", "tp", "teleport", "summon", "effect", "attribute", "enchant", "experience", "xp", "gamemode", "difficulty", "team", "scoreboard", "title", "bossbar", "execute", "if", "unless", "as", "at", "positioned", "facing", "rotated", "anchored", "align", "run", "store", "result", "success", "data", "merge", "modify", "get", "remove", "time", "weather", "daylock", "gamerule", "seed", "setblock", "fill", "clone", "loot", "place", "structure", "forceload", "function", "schedule", "reload", "debug", "perf", "version", "stop", "save-all", "save-on", "save-off", "publish", "op", "deop", "whitelist", "ban", "pardon", "kick", "list", "locate", "locatebiome", "spawnpoint", "worldborder", "particle", "playsound", "stopsound", "tag", "advancement", "recipe", "spectate", "camera", "random", "testfor", "testforblock", "testforblocks", "block", "blocks", "entity", "score", "predicate", "biome", "dimension", "@p", "@a", "@r", "@s", "@e", "true", "false", "matches", "distance", "level", "limit", "sort", "type", "name", "nbt"};

    public static final String[] CUSTOM_COMMAND_KEYWORDS = {
            "sleep", "repeat"
    };

    public static final String[] NBT_KEYWORDS = {
            "CustomName", "Color", "ActiveEffects", "Duration",
            "Amplifier", "Enchantments", "AttributeModifiers",
            "Name", "Amount", "Operation", "UUID"
    };

    public static final String COMMENT_START_CHAR = "#";
    public static final String CUSTOM_COMMAND_START_CHAR = "%";
    public static final String MINECRAFT_COMMAND_START_CHAR = "/";
    public static final String INDENT = "    ";

    public static boolean isPartOfWord(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    public static List<String> parseArguments(String line) {
        List<String> args = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }

            if (c == ' ' && !inQuotes) {
                if (!current.isEmpty()) {
                    args.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            args.add(current.toString());
        }

        return args;
    }

    public static boolean isInteger(String s) {
        if (s == null || s.isEmpty()) return false;

        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String getWordAt(String line, int index) {
        if (!isPartOfWord(line.charAt(index))) return null;

        int start = index;
        int end = index;

        while (start > 0 && isPartOfWord(line.charAt(start - 1))) start--;
        while (end < line.length() - 1 && isPartOfWord(line.charAt(end + 1))) end++;

        return line.substring(start, end + 1);
    }

    public static boolean isKeyword(String word, String[] keywords) {
        if (word == null) return false;
        for (String kw : keywords) {
            if (kw.equals(word)) return true;
        }
        return false;
    }

    public static boolean isQuote(String line, int index) {
        return line.charAt(index) == '"' &&
                (index == 0 || line.charAt(index - 1) != '\\');
    }

    public static boolean isInsideString(String line, int index) {
        boolean inside = false;
        for (int i = 0; i < index; i++) {
            if (line.charAt(i) == '"' && (i == 0 || line.charAt(i - 1) != '\\')) {
                inside = !inside;
            }
        }
        return inside;
    }

    public static boolean isNBTKey(String line, int index) {
        if (!Character.isLetter(line.charAt(index))) return false;

        int i = index;
        while (i < line.length() && Character.isLetter(line.charAt(i))) {
            i++;
        }

        return i < line.length() && line.charAt(i) == ':';
    }

    public static boolean isComment(String trimmedLine) {
        if (trimmedLine.isBlank()) return false;
        return trimmedLine.charAt(0) == COMMENT_START_CHAR.charAt(0);
    }

    public static void endCorrespondingFrames(List<CommandLine> programLines, List<ExecutionFrame> frames, int currentIndentLevel, int lastExecutableIndex) {
        for (ExecutionFrame frame : frames) {

            int lastExecutableIndentLevel = programLines.get(lastExecutableIndex).indentLevel();
            if (frame.endIndex != -1 || frame.startIndex == -1) {
//                Multicommandblock.LOGGER.info("Skipping frame with enterIndex: {} for currentIndentLevel {} and frame.indentLevel {} and lastExecutableIndentLevel: {}", frame.enterIndex + 1, currentIndentLevel, frame.childIndentLevel, lastExecutableIndentLevel);
                continue;
            }

            if (frame.childIndentLevel > currentIndentLevel && lastExecutableIndex >= frame.enterIndex) {
                if (lastExecutableIndex == frame.enterIndex) {
                    frame.endIndex = lastExecutableIndex;
                    frame.revolutionsCount = 0;
                } else
                    for (int i = lastExecutableIndex; i >= frame.enterIndex; i--) {
                        if (programLines.get(i).indentLevel() == frame.childIndentLevel && programLines.get(i).isExecutable()) {
                            frame.endIndex = i;
                            break;
                        }
                    }
//                Multicommandblock.LOGGER.info("2Ended enterIndex: {} on endIndex: {} for currentIndentLevel {} and frame.indentLevel {} and lastExecutableIndentLevel: {}", frame.enterIndex + 1, frame.endIndex+1, currentIndentLevel, frame.childIndentLevel, lastExecutableIndentLevel);

            } else {
//                Multicommandblock.LOGGER.info("3NotEnding enterIndex: {} on endIndex: {} for currentIndentLevel {} and frame.indentLevel {} and lastExecutableIndentLevel: {}", frame.enterIndex + 1, 0, currentIndentLevel, frame.childIndentLevel, lastExecutableIndentLevel);
            }
        }
    }

}
