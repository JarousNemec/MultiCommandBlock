package org.jardathedev.multicommandblock.util;

import java.util.ArrayList;
import java.util.List;

public class commandUtil {

    public static final String[] MINECRAFT_COMMAND_KEYWORDS = { "help","say","tell","msg","w","me","trigger","give","clear","kill","tp","teleport","summon","effect","attribute","enchant","experience","xp","gamemode","difficulty","team","scoreboard","title","bossbar","execute","if","unless","as","at","positioned","facing","rotated","anchored","align","run","store","result","success","data","merge","modify","get","remove","time","weather","daylock","gamerule","seed","setblock","fill","clone","loot","place","structure","forceload","function","schedule","reload","debug","perf","version","stop","save-all","save-on","save-off","publish","op","deop","whitelist","ban","pardon","kick","list","locate","locatebiome","spawnpoint","worldborder","particle","playsound","stopsound","tag","advancement","recipe","spectate","camera","random","testfor","testforblock","testforblocks","block","blocks","entity","score","predicate","biome","dimension","@p","@a","@r","@s","@e","true","false","matches","distance","level","limit","sort","type","name","nbt" };

    public static final String[] CUSTOM_COMMAND_KEYWORDS = {
            "sleep"
    };

    public static final String[] NBT_KEYWORDS = {
            "CustomName", "Color", "ActiveEffects", "Duration",
            "Amplifier", "Enchantments", "AttributeModifiers",
            "Name", "Amount", "Operation", "UUID"
    };

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

}
