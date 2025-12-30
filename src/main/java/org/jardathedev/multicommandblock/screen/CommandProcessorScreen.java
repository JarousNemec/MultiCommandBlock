package org.jardathedev.multicommandblock.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jardathedev.multicommandblock.registry.ModPackets;
import org.jardathedev.multicommandblock.screen.textEditor.SyntaxHighlighter;
import org.jardathedev.multicommandblock.screen.textEditor.TextEditor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class CommandProcessorScreen extends Screen {
    private boolean initialized = false;

    private int scrollY = 0;
    private int scrollX = 0;

    private int editorX;
    private int editorY;

    private int lineNumberWidth;

    private static final int TERM_COLUMNS = 64;
    private static final int TERM_ROWS = 20;
    private static final int LINE_HEIGHT = 10;
    private static final int PADDING = 6;

    private boolean dirty = false;

    private int editorWidth;
    private int editorHeight;
    private int charWidth;

    private final BlockPos pos;
    private final List<String> initialLines;
    private final List<Integer> invalidLines;
    private final TextEditor textEditor;

    public CommandProcessorScreen(BlockPos pos, List<String> initialLines, List<Integer> invalidLines) {
        super(Text.empty());
        this.pos = pos;
        this.initialLines = initialLines;
        this.invalidLines = invalidLines;
        textEditor = new TextEditor();
    }


    @Override
    protected void init() {
        super.init();

        initDimensions();

        initEditorOnce();

        ensureCursorVisible();
    }

    private void initEditorOnce() {
        if (!initialized) {
            if (initialLines.isEmpty())
                textEditor.init(new ArrayList<>());
            else
                textEditor.init(initialLines);

            initialized = true;
        }
    }

    private void initDimensions() {
        charWidth = textRenderer.getWidth("W");
        int digits = String.valueOf(textEditor.getLinesCount()).length();
        lineNumberWidth = textRenderer.getWidth("0".repeat(digits)) + 6;
        editorWidth =
                lineNumberWidth +
                        TERM_COLUMNS * charWidth +
                        PADDING * 3;


        editorHeight =
                TERM_ROWS * LINE_HEIGHT + PADDING * 2;


        editorX = (width - editorWidth) / 2;
        editorY = (height - editorHeight) / 2;
    }

    @Override
    public void close() {
        super.close();

        if (!dirty) return;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);

        buf.writeInt(textEditor.getLinesCount());
        for (String line : textEditor.getLines()) {
            buf.writeString(line);
        }

        ClientPlayNetworking.send(ModPackets.SAVE_LINES, buf);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawBackground(context);

        int textX = editorX + PADDING * 2 + lineNumberWidth;
        int textY = editorY + PADDING;

        drawLineNumbers(context);

        drawSelection(context, textX, textY);

        drawMainText(context, textX, textY);

        drawCursor(context, textX, textY, textEditor.getLinesCount());
    }

    private void drawSelection(DrawContext context, int textX, int textY) {
        if (!textEditor.hasSelection()) return;

        int startLine = textEditor.getSelStartLine();
        int startCol = textEditor.getSelStartCol();
        int endLine = textEditor.getSelEndLine();
        int endCol = textEditor.getSelEndCol();

        for (int line = startLine; line <= endLine; line++) {

            int visibleLine = line - scrollY;
            if (visibleLine < 0 || visibleLine >= TERM_ROWS) continue;

            int fromCol = (line == startLine) ? startCol : 0;
            int toCol = (line == endLine)
                    ? endCol
                    : textEditor.getLine(line).length();

            int visibleFrom = Math.max(fromCol - scrollX, 0);
            int visibleTo = Math.min(toCol - scrollX, TERM_COLUMNS);

            if (visibleFrom >= visibleTo) continue;

            int x1 = textX + visibleFrom * charWidth;
            int y1 = textY + visibleLine * LINE_HEIGHT;
            int x2 = textX + visibleTo * charWidth;
            int y2 = y1 + LINE_HEIGHT;

            context.fill(x1, y1, x2, y2, 0x803388FF); // translucent blue
        }
    }


    private void drawLineNumbers(DrawContext context) {
        for (int row = 0; row < TERM_ROWS; row++) {
            int lineIndex = scrollY + row;
            if (lineIndex >= textEditor.getLinesCount()) break;

            String number = String.valueOf(lineIndex + 1);

            int numberX =
                    editorX + lineNumberWidth - textRenderer.getWidth(number) + PADDING;
            int numberY =
                    editorY + PADDING + row * LINE_HEIGHT;
            int color = invalidLines.contains(lineIndex)
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
        for (int row = 0; row < TERM_ROWS; row++) {
            int lineIndex = scrollY + row;
            if (lineIndex >= textEditor.getLinesCount()) break;

            String line = textEditor.getLine(lineIndex);

            for (int col = 0; col < TERM_COLUMNS; col++) {
                int charIndex = scrollX + col;
                if (charIndex >= line.length()) break;

                char c = line.charAt(charIndex);
                int color = SyntaxHighlighter.getColor(line, charIndex);

                context.drawText(
                        textRenderer,
                        String.valueOf(c),
                        textX + col * charWidth,
                        textY + row * LINE_HEIGHT,
                        color,
                        false
                );
            }
        }
    }

    private void drawBackground(DrawContext context) {
        // průhledné pozadí (lehké ztmavení světa)
        context.fill(0, 0, width, height, 0x88000000);

        // černý editor
        context.fill(
                editorX,
                editorY,
                editorX + editorWidth,
                editorY + editorHeight,
                0xFF000000
        );

        // pozadí pro čísla řádků
        context.fill(
                editorX,
                editorY,
                editorX + lineNumberWidth + PADDING,
                editorY + editorHeight,
                0xFF101010
        );
    }

    private void drawCursor(DrawContext context, int textX, int textY, int maxLines) {
        if ((System.currentTimeMillis() / 500) % 2 != 0) return;

        if (textEditor.getCursorLine() < scrollY || textEditor.getCursorLine() >= scrollY + CommandProcessorScreen.TERM_ROWS)
            return;
        if (textEditor.getCursorLine() < 0 || textEditor.getCursorLine() >= maxLines) return;

        int visibleColumn = textEditor.getCursorColumn() - scrollX;
        if (visibleColumn < 0 || visibleColumn >= TERM_COLUMNS) return;

        int cursorX = textX + visibleColumn * charWidth;
        int cursorY = textY + (textEditor.getCursorLine() - scrollY) * LINE_HEIGHT;


        context.fill(
                cursorX,
                cursorY,
                cursorX + 1,
                cursorY + LINE_HEIGHT,
                0xFFFFFFFF
        );
    }

    private void ensureCursorVisible() {
        textEditor.clampCursor();

        // horizontální
        if (textEditor.getCursorColumn() < scrollX) {
            scrollX = textEditor.getCursorColumn();
        } else if (textEditor.getCursorColumn() >= scrollX + TERM_COLUMNS) {
            scrollX = textEditor.getCursorColumn() - TERM_COLUMNS + 1;
        }

        // vertikální
        if (textEditor.getCursorLine() < scrollY) {
            scrollY = textEditor.getCursorLine();
        } else if (textEditor.getCursorLine() >= scrollY + TERM_ROWS) {
            scrollY = textEditor.getCursorLine() - TERM_ROWS + 1;
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (chr >= 32) {
            textEditor.typeChar(chr);
            dirty = true;
            ensureCursorVisible();
            return true;
        }
        return false;
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        boolean shiftDown = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

        boolean ctrlDown =
                InputUtil.isKeyPressed(
                        MinecraftClient.getInstance().getWindow().getHandle(),
                        GLFW.GLFW_KEY_LEFT_CONTROL
                ) ||
                        InputUtil.isKeyPressed(
                                MinecraftClient.getInstance().getWindow().getHandle(),
                                GLFW.GLFW_KEY_RIGHT_CONTROL
                        );

        if (ctrlDown) {
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
                        dirty = true;
                        ensureCursorVisible();
                    }
                    return true;
                }

                // CTRL + V
                case GLFW.GLFW_KEY_V -> {
                    String clipboard = MinecraftClient.getInstance().keyboard.getClipboard();
                    if (!clipboard.isEmpty()) {
                        textEditor.pasteText(clipboard);
                        dirty = true;
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
                if (shiftDown) textEditor.startSelection();
                else textEditor.clearSelection();

                textEditor.moveCursorHorizontal(-1);
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (shiftDown) textEditor.startSelection();
                else textEditor.clearSelection();

                textEditor.moveCursorHorizontal(1);
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                if (shiftDown) textEditor.startSelection();
                else textEditor.clearSelection();

                textEditor.moveCursorVertical(-1);
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                if (shiftDown) textEditor.startSelection();
                else textEditor.clearSelection();

                textEditor.moveCursorVertical(1);
                ensureCursorVisible();
                return true;
            }

            case GLFW.GLFW_KEY_ENTER -> {
                textEditor.applyEnter();
                dirty = true;
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                textEditor.applyBackspace();
                dirty = true;
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                textEditor.applyDelete();
                dirty = true;
                ensureCursorVisible();
                return true;
            }

            case GLFW.GLFW_KEY_HOME -> {
                if (shiftDown) textEditor.startSelection();
                else textEditor.clearSelection();

                textEditor.applyHome();
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_END -> {
                if (shiftDown) textEditor.startSelection();
                else textEditor.clearSelection();

                textEditor.applyEnd();
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_PAGE_UP -> {
                if (shiftDown) textEditor.startSelection();
                else textEditor.clearSelection();

                textEditor.moveCursorVertical(-TERM_ROWS);
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_PAGE_DOWN -> {
                if (shiftDown) textEditor.startSelection();
                else textEditor.clearSelection();

                textEditor.moveCursorVertical(TERM_ROWS);
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_TAB -> {
                if (shiftDown) {
                    textEditor.outdent();
                } else {
                    textEditor.indent();
                }

                dirty = true;
                ensureCursorVisible();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

}
