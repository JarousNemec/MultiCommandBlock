package org.jardathedev.multicommandblock.screen.textEditorWidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.InputUtil;
import org.jardathedev.multicommandblock.screen.textEditor.SyntaxHighlighter;
import org.jardathedev.multicommandblock.screen.textEditor.TextEditor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class TextEditorWidget extends ClickableWidget {
    private boolean initialized = false;

    private int scrollY = 0;
    private int scrollX = 0;

    private boolean hasChanged = false;

    private final TextEditor textEditor;
    private final TextRenderer textRenderer;
    private final TextEditorWidgetProps props;

    public TextEditorWidget(TextEditorWidgetProps props, TextRenderer textRenderer) {
        super(props.x, props.y, props.width, props.height, props.message);
        this.props = props;
        this.textEditor = new TextEditor();
        this.textRenderer = textRenderer;
        initEditorOnce();

        ensureCursorVisible();
    }

    private void initEditorOnce() {
        if (!initialized) {
            if (props.initialLines.isEmpty())
                textEditor.init(new ArrayList<>());
            else
                textEditor.init(props.initialLines);

            initialized = true;
        }
    }

    public TextEditor getTextEditor() {
        return textEditor;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        drawBackground(context);

        int textX = getX() + props.padding * 2 + props.lineNumberWidth;
        int textY = getY() + props.padding;

        drawLineNumbers(context);

        drawSelection(context, textX, textY);

        drawMainText(context, textX, textY);

        drawCursor(context, textX, textY, textEditor.getLinesCount());
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.setFocused(true);
    }

    @Override
    public boolean isFocused() {
        return super.isFocused();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    private void drawSelection(DrawContext context, int textX, int textY) {
        if (!textEditor.hasSelection()) return;

        int startLine = textEditor.getSelStartLine();
        int startCol = textEditor.getSelStartCol();
        int endLine = textEditor.getSelEndLine();
        int endCol = textEditor.getSelEndCol();

        for (int line = startLine; line <= endLine; line++) {

            int visibleLine = line - scrollY;
            if (visibleLine < 0 || visibleLine >= props.termRows) continue;

            int fromCol = (line == startLine) ? startCol : 0;
            int toCol = (line == endLine)
                    ? endCol
                    : textEditor.getLine(line).length();

            int visibleFrom = Math.max(fromCol - scrollX, 0);
            int visibleTo = Math.min(toCol - scrollX, props.termColumns);

            if (visibleFrom >= visibleTo) continue;

            int x1 = textX + visibleFrom * props.charWidth;
            int y1 = textY + visibleLine * props.lineHeight;
            int x2 = textX + visibleTo * props.charWidth;
            int y2 = y1 + props.lineHeight;

            context.fill(x1, y1, x2, y2, 0x803388FF); // translucent blue
        }
    }


    private void drawLineNumbers(DrawContext context) {
        for (int row = 0; row < props.termRows; row++) {
            int lineIndex = scrollY + row;
            if (lineIndex >= textEditor.getLinesCount()) break;

            String number = String.valueOf(lineIndex);

            int numberX =
                    getX() + props.lineNumberWidth - textRenderer.getWidth(number) + props.padding;
            int numberY =
                    getY() + props.padding + row * props.lineHeight;
            int color = props.invalidLines.contains(lineIndex)
                    ? 0xFFFF5555
                    : 0xFF888888;

            context.drawText(
                    textRenderer,
                    number,
                    numberX,
                    numberY
                    , color,
                    false
            );
        }
    }

    private void drawMainText(DrawContext context, int textX, int textY) {
        for (int row = 0; row < props.termRows; row++) {
            int lineIndex = scrollY + row;
            if (lineIndex >= textEditor.getLinesCount()) break;

            String line = textEditor.getLine(lineIndex);

            for (int col = 0; col < props.termColumns; col++) {
                int charIndex = scrollX + col;
                if (charIndex >= line.length()) break;

                char c = line.charAt(charIndex);
                int color = SyntaxHighlighter.getColor(line, charIndex);

                context.drawText(
                        textRenderer,
                        String.valueOf(c),
                        textX + col * props.charWidth,
                        textY + row * props.lineHeight,
                        color,
                        false
                );
            }
        }
    }

    private void drawBackground(DrawContext context) {
        // černý editor
        context.fill(
                getX(),
                getY(),
                getX() + getWidth(),
                getY() + getHeight(),
                0xFF000000
        );

        // pozadí pro čísla řádků
        context.fill(
                getX(),
                getY(),
                getX() + props.lineNumberWidth + props.padding,
                getY() + getHeight(),
                0xFF101010
        );
    }

    private void drawCursor(DrawContext context, int textX, int textY, int maxLines) {
        if ((System.currentTimeMillis() / 500) % 2 != 0) return;

        if (textEditor.getCursorLine() < scrollY || textEditor.getCursorLine() >= scrollY + props.termRows)
            return;
        if (textEditor.getCursorLine() < 0 || textEditor.getCursorLine() >= maxLines) return;

        int visibleColumn = textEditor.getCursorColumn() - scrollX;
        if (visibleColumn < 0 || visibleColumn >= props.termColumns) return;

        int cursorX = textX + visibleColumn * props.charWidth;
        int cursorY = textY + (textEditor.getCursorLine() - scrollY) * props.lineHeight;


        context.fill(
                cursorX,
                cursorY,
                cursorX + 1,
                cursorY + props.lineHeight,
                0xFFFFFFFF
        );
    }

    private void ensureCursorVisible() {
        textEditor.clampCursor();

        // horizontální
        if (textEditor.getCursorColumn() < scrollX) {
            scrollX = textEditor.getCursorColumn();
        } else if (textEditor.getCursorColumn() >= scrollX + props.termColumns) {
            scrollX = textEditor.getCursorColumn() - props.termColumns + 1;
        }

        // vertikální
        if (textEditor.getCursorLine() < scrollY) {
            scrollY = textEditor.getCursorLine();
        } else if (textEditor.getCursorLine() >= scrollY + props.termRows) {
            scrollY = textEditor.getCursorLine() - props.termRows + 1;
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!this.isFocused()) return false;
        if (chr >= 32) {
            textEditor.typeChar(chr);
            hasChanged = true;
            ensureCursorVisible();
            return true;
        }
        return false;
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isFocused()) return false;
        boolean isShiftDown = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

        boolean isCtrlDown =
                InputUtil.isKeyPressed(
                        MinecraftClient.getInstance().getWindow().getHandle(),
                        GLFW.GLFW_KEY_LEFT_CONTROL
                ) ||
                        InputUtil.isKeyPressed(
                                MinecraftClient.getInstance().getWindow().getHandle(),
                                GLFW.GLFW_KEY_RIGHT_CONTROL
                        );

        if (isCtrlDown) {
            switch (keyCode) {

                // CTRL + C
                case GLFW.GLFW_KEY_C -> {
                    if (textEditor.hasSelection()) {
                        String text = textEditor.getSelectedText();
                        MinecraftClient.getInstance().keyboard.setClipboard(text);
                    }
                    return true;
                }

                // CTRL + X
                case GLFW.GLFW_KEY_X -> {
                    if (textEditor.hasSelection()) {
                        String text = textEditor.getSelectedText();
                        MinecraftClient.getInstance().keyboard.setClipboard(text);
                        textEditor.deleteSelection();
                        hasChanged = true;
                        ensureCursorVisible();
                    }
                    return true;
                }

                // CTRL + V
                case GLFW.GLFW_KEY_V -> {
                    String clipboard = MinecraftClient.getInstance().keyboard.getClipboard();
                    if (!clipboard.isEmpty()) {
                        textEditor.pasteText(clipboard);
                        hasChanged = true;
                        ensureCursorVisible();
                    }
                    return true;
                }

                // CTRL + A
                case GLFW.GLFW_KEY_A -> {
                    textEditor.selectAll();
                    ensureCursorVisible();
                    return true;
                }
            }
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT -> {
                if (isShiftDown) textEditor.startSelection();
                else textEditor.clearSelection();

                textEditor.moveCursorHorizontal(-1);
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (isShiftDown) textEditor.startSelection();
                else textEditor.clearSelection();

                textEditor.moveCursorHorizontal(1);
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                if (isShiftDown) textEditor.startSelection();
                else textEditor.clearSelection();

                textEditor.moveCursorVertical(-1);
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                if (isShiftDown) textEditor.startSelection();
                else textEditor.clearSelection();

                textEditor.moveCursorVertical(1);
                ensureCursorVisible();
                return true;
            }

            case GLFW.GLFW_KEY_ENTER -> {
                textEditor.applyEnter();
                hasChanged = true;
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                textEditor.applyBackspace();
                hasChanged = true;
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                textEditor.applyDelete();
                hasChanged = true;
                ensureCursorVisible();
                return true;
            }

            case GLFW.GLFW_KEY_HOME -> {
                if (isShiftDown) textEditor.startSelection();
                else textEditor.clearSelection();

                textEditor.applyHome();
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_END -> {
                if (isShiftDown) textEditor.startSelection();
                else textEditor.clearSelection();

                textEditor.applyEnd();
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_PAGE_UP -> {
                if (isShiftDown) textEditor.startSelection();
                else textEditor.clearSelection();

                textEditor.moveCursorVertical(-props.termRows);
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_PAGE_DOWN -> {
                if (isShiftDown) textEditor.startSelection();
                else textEditor.clearSelection();

                textEditor.moveCursorVertical(props.termRows);
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_TAB -> {
                if (isShiftDown) {
                    textEditor.outdent();
                } else {
                    textEditor.indent();
                }

                hasChanged = true;
                ensureCursorVisible();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
