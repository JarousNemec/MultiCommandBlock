package org.jardathedev.multicommandblock.screen.textEditor;

import java.util.ArrayList;
import java.util.List;

public class TextEditor {
    private final List<String> lines;
    private int cursorLine;
    private int cursorColumn;

    // === SELECTION ===
    private boolean hasSelection = false;

    private int selStartLine;
    private int selStartCol;
    private int selEndLine;
    private int selEndCol;

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

    public int getLinesCount() {
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

    public void addLineAtIndex(int lineIndex, String line) {
        lines.add(lineIndex, line);
    }
    public void addLine(String line) {
        lines.add(line);
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
        updateSelection();
    }

    public void moveCursorHorizontal(int step) {
        cursorColumn += step;
        clampCursor();
        updateSelection();
    }

    public void typeChar(char chr) {
        if (hasSelection) {
            deleteSelection();
        }
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
        if (hasSelection) {
            deleteSelection();
        }
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
        if (hasSelection) {
            deleteSelection();
        }
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
        if (hasSelection) {
            deleteSelection();
        }
        clampCursor();
        if (cursorColumn < lines.get(cursorLine).length()) {
            String newLineText = getCurrentLine().substring(0, cursorColumn) + getCurrentLine().substring(cursorColumn + 1);
            setLine(cursorLine, newLineText);
        } else if (cursorColumn == getCurrentLine().length() && cursorLine < getLinesCount() - 1) {
            String newLineText = String.join("", getCurrentLine(), getLine(cursorLine + 1));
            setLine(cursorLine, newLineText);
            removeLine(cursorLine + 1);
        }
    }

    public void applyHome() {
        clampCursor();
        cursorColumn = 0;
        updateSelection();
    }

    public void applyEnd() {
        clampCursor();
        cursorColumn = getCurrentLine().length();
        updateSelection();
    }

    public int getSelStartLine() {
        return (selStartLine < selEndLine ||
                (selStartLine == selEndLine && selStartCol <= selEndCol))
                ? selStartLine : selEndLine;
    }

    public int getSelStartCol() {
        return (selStartLine < selEndLine ||
                (selStartLine == selEndLine && selStartCol <= selEndCol))
                ? selStartCol : selEndCol;
    }

    public int getSelEndLine() {
        return (selStartLine < selEndLine ||
                (selStartLine == selEndLine && selStartCol <= selEndCol))
                ? selEndLine : selStartLine;
    }

    public int getSelEndCol() {
        return (selStartLine < selEndLine ||
                (selStartLine == selEndLine && selStartCol <= selEndCol))
                ? selEndCol : selStartCol;
    }

    public boolean hasSelection() {
        return hasSelection;
    }

    public void clearSelection() {
        hasSelection = false;
    }

    public void startSelection() {
        if (!hasSelection) {
            selStartLine = cursorLine;
            selStartCol = cursorColumn;
            selEndLine = cursorLine;
            selEndCol = cursorColumn;
            hasSelection = true;
        }
    }

    public void updateSelection() {
        if (!hasSelection) return;

        selEndLine = cursorLine;
        selEndCol = cursorColumn;
    }

    public void selectAll() {
        if (lines.isEmpty()) return;

        selStartLine = 0;
        selStartCol = 0;

        selEndLine = getLinesCount() - 1;
        selEndCol = getLine(selEndLine).length();

        cursorLine = selEndLine;
        cursorColumn = selEndCol;

        hasSelection = true;
    }


    public String getSelectedText() {
        if (!hasSelection) return "";

        int startLine = getSelStartLine();
        int startCol = getSelStartCol();
        int endLine = getSelEndLine();
        int endCol = getSelEndCol();

        StringBuilder sb = new StringBuilder();

        for (int line = startLine; line <= endLine; line++) {
            String textLine = getLine(line);

            int from = (line == startLine) ? startCol : 0;
            int to = (line == endLine) ? endCol : textLine.length();

            sb.append(textLine, from, to);

            if (line < endLine) sb.append("\n");
        }

        return sb.toString();
    }

    public void deleteSelection() {
        if (!hasSelection) return;

        int startLine = getSelStartLine();
        int startCol = getSelStartCol();
        int endLine = getSelEndLine();
        int endCol = getSelEndCol();

        if (startLine == endLine) {
            String line = getLine(startLine);
            setLine(
                    startLine,
                    line.substring(0, startCol) + line.substring(endCol)
            );
        } else {
            String first = getLine(startLine).substring(0, startCol);
            String last = getLine(endLine).substring(endCol);

            for (int i = endLine; i > startLine; i--) {
                removeLine(i);
            }

            setLine(startLine, first + last);
        }

        cursorLine = startLine;
        cursorColumn = startCol;
        clearSelection();
        clampCursor();
    }

    public void pasteText(String text) {
        if (hasSelection) {
            deleteSelection();
        }

        clampCursor();

        text = text.replace("\r\n", "\n").replace("\r", "\n");

        String[] split = text.split("\n", -1);

        String line = getCurrentLine();
        String before = line.substring(0, cursorColumn);
        String after = line.substring(cursorColumn);

        if (split.length == 1) {
            setCurrentLine(before + split[0] + after);
            cursorColumn += split[0].length();
            return;
        }

        setCurrentLine(before + split[0]);

        int insertLine = cursorLine;

        for (int i = 1; i < split.length; i++) {
            insertLine++;

            if (insertLine > lines.size()) {
                addLine(split[i]);
            } else {
                addLineAtIndex(insertLine, split[i]);
            }
        }

        cursorLine = insertLine;
        setCurrentLine(getCurrentLine() + after);
        cursorColumn = split[split.length - 1].length();

        clampCursor();
    }


}
