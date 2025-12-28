package org.jardathedev.multicommandblock.screen;

import java.util.ArrayList;
import java.util.List;

public class TextEditor {
    private final List<String> lines;
    private int cursorLine;
    private int cursorColumn;

    public TextEditor() {
        this.lines = new ArrayList<>();
        cursorLine = 0;
        cursorColumn = 0;
    }

    public int getCursorLine() {
        return cursorLine;
    }

    public int getCursorColumn() {
        return cursorColumn;
    }

    public void init(List<String> initLines) {
        lines.clear();
        lines.addAll(initLines);
        if (lines.isEmpty()) {
            lines.add("");
        }
        cursorLine = 0;
        cursorColumn = 0;
    }

    public List<String> getLines() {
        return lines;
    }

    public int lineCount() {
        return lines.size();
    }

    public String getLine(int lineIndex) {
        return lines.get(lineIndex);
    }

    public void setLine(int lineIndex, String line) {
        lines.set(lineIndex, line);
    }

    public void addLineAfterCurrent(String line) {
        lines.add(cursorLine + 1, line);
    }

    public String getCurrentLine() {
        return lines.get(cursorLine);
    }

    public void setCurrentLine(String text) {
        lines.set(cursorLine, text);
    }

    public void removeCurrentLine() {
        lines.remove(cursorLine);
    }
    public void removeLine(int lineIndex) {
        lines.remove(lineIndex);
    }

    public void clampCursor() {
        if (lines.isEmpty()) {
            lines.add("");
            cursorLine = 0;
            cursorColumn = 0;
            return;
        }

        if (cursorLine < 0) cursorLine = 0;
        if (cursorLine >= lines.size()) cursorLine = lines.size() - 1;

        int lineLength = getCurrentLine().length();
        if (cursorColumn < 0) cursorColumn = 0;
        if (cursorColumn > lineLength) cursorColumn = lineLength;
    }


    public void moveCursorVertical(int step) {
        cursorLine += step;
        clampCursor();
    }

    public void moveCursorHorizontal(int step) {
        cursorColumn += step;
        clampCursor();
    }

    public void typeChar(char chr) {
        clampCursor();
        String line = getCurrentLine();
        String newLine =
                line.substring(0, cursorColumn) +
                        chr +
                        line.substring(cursorColumn);

        setCurrentLine(newLine);
        cursorColumn++;
    }

    public void applyEnter() {
        clampCursor();
        String line = getCurrentLine();

        String before = line.substring(0, cursorColumn);
        String after = line.substring(cursorColumn);


        setCurrentLine(before);
        addLineAfterCurrent(after);

        cursorLine++;
        cursorColumn = 0;
    }

    public void applyBackspace() {
        clampCursor();
        if (cursorColumn > 0) {
            String line = getCurrentLine();
            setCurrentLine(line.substring(0, cursorColumn - 1) + line.substring(cursorColumn));
            cursorColumn--;
        } else if (cursorLine > 0) {
            int prevLen = getLine(cursorLine - 1).length();
            setLine(cursorLine - 1, lines.get(cursorLine - 1) + lines.get(cursorLine));

            removeCurrentLine();
            cursorLine--;
            cursorColumn = prevLen;
        }
    }

    public void applyDelete() {
        clampCursor();
        if (cursorColumn < lines.get(cursorLine).length()) {
            String newLineText = getCurrentLine().substring(0, cursorColumn) + getCurrentLine().substring(cursorColumn + 1);
            setLine(cursorLine, newLineText);
        } else if (cursorColumn == getCurrentLine().length() && cursorLine < lineCount() - 1) {
            String newLineText = String.join("", getCurrentLine(), getLine(cursorLine + 1));
            setLine(cursorLine, newLineText);
            removeLine(cursorLine + 1);
        }
    }

    public void applyHome() {
        clampCursor();
        cursorColumn = 0;
    }

    public void applyEnd() {
        clampCursor();
        cursorColumn = getCurrentLine().length();
    }

}
