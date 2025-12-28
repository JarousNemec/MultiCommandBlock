package org.jardathedev.multicommandblock.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import org.jardathedev.multicommandblock.entity.CommandProcessorBlockEntity;
import org.jardathedev.multicommandblock.registry.ModPackets;
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

    private static final int TERM_COLUMNS = 100;
    private static final int TERM_ROWS = 25;
    private static final int LINE_HEIGHT = 10;
    private static final int PADDING = 6;

    private boolean dirty = false;

    private int editorWidth;
    private int editorHeight;
    private int charWidth;

    private final CommandProcessorBlockEntity blockEntity;
    private final TextEditor textEditor;

    public CommandProcessorScreen(CommandProcessorBlockEntity blockEntity) {
        super(Text.empty());
        this.blockEntity = blockEntity;
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
            //      todo: delete for prod
            List<String> commands = new ArrayList<>();
            commands.add("/summon sheep ~ ~ ~ {Color:0,CustomName:'{\"text\":\"DUHOVKA\",\"color\":\"light_purple\"}',ActiveEffects:[{Id:1,Amplifier:1,Duration:999999,ShowParticles:0}]}");
            commands.add("");
            commands.add("/summon lightning_bolt");
            commands.add("/give @p diamond_sword{display:{Name:'{\"text\":\"Thorův meč\",\"color\":\"aqua\"}'},Enchantments:[{id:sharpness,lvl:5}],AttributeModifiers:[{AttributeName:\"generic.attack_damage\",Name:\"generic.attack_damage\",Amount:10,Operation:0,UUID:[I;1,2,3,4]}]} 1");
            commands.add("");
            commands.add("/execute as @a at @s if entity @s[nbt={OnGround:0b}] run summon firework_rocket ~ ~ ~ {LifeTime:10}\n");
            commands.add("/summon sheep ~ ~ ~ {Color:0,CustomName:'{\"text\":\"DUHOVKA\",\"color\":\"light_purple\"}',ActiveEffects:[{Id:1,Amplifier:1,Duration:999999,ShowParticles:0}]}");
            commands.add("");
            commands.add("/summon lightning_bolt");
            commands.add("/give @p diamond_sword{display:{Name:'{\"text\":\"Thorův meč\",\"color\":\"aqua\"}'},Enchantments:[{id:sharpness,lvl:5}],AttributeModifiers:[{AttributeName:\"generic.attack_damage\",Name:\"generic.attack_damage\",Amount:10,Operation:0,UUID:[I;1,2,3,4]}]} 1");
            commands.add("");
            commands.add("/execute as @a at @s if entity @s[nbt={OnGround:0b}] run summon firework_rocket ~ ~ ~ {LifeTime:10}\n");
            commands.add("/summon sheep ~ ~ ~ {Color:0,CustomName:'{\"text\":\"DUHOVKA\",\"color\":\"light_purple\"}',ActiveEffects:[{Id:1,Amplifier:1,Duration:999999,ShowParticles:0}]}");
            commands.add("");
            commands.add("/summon lightning_bolt");
            commands.add("/give @p diamond_sword{display:{Name:'{\"text\":\"Thorův meč\",\"color\":\"aqua\"}'},Enchantments:[{id:sharpness,lvl:5}],AttributeModifiers:[{AttributeName:\"generic.attack_damage\",Name:\"generic.attack_damage\",Amount:10,Operation:0,UUID:[I;1,2,3,4]}]} 1");
            commands.add("");
            commands.add("/execute as @a at @s if entity @s[nbt={OnGround:0b}] run summon firework_rocket ~ ~ ~ {LifeTime:10}\n");
            if (blockEntity.getLines().isEmpty())
                textEditor.init(commands);
            else
                textEditor.init(blockEntity.getLines());

            initialized = true;
        }
    }

    private void initDimensions() {
        charWidth = textRenderer.getWidth("W");
        int digits = String.valueOf(textEditor.lineCount()).length();
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
        buf.writeBlockPos(blockEntity.getPos());

        buf.writeInt(textEditor.lineCount());
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

        drawMainText(context, textX, textY);

        drawCursor(context, textX, textY, textEditor.lineCount(), TERM_ROWS);
    }

    private void drawLineNumbers(DrawContext context) {
        for (int row = 0; row < TERM_ROWS; row++) {
            int lineIndex = scrollY + row;
            if (lineIndex >= textEditor.lineCount()) break;

            String number = String.valueOf(lineIndex + 1);

            int numberX =
                    editorX + lineNumberWidth - textRenderer.getWidth(number) + PADDING;
            int numberY =
                    editorY + PADDING + row * LINE_HEIGHT;

            context.drawText(
                    textRenderer,
                    number,
                    numberX,
                    numberY,
                    0xFF888888,
                    false
            );
        }
    }

    private void drawMainText(DrawContext context, int textX, int textY) {
        for (int row = 0; row < TERM_ROWS; row++) {
            int lineIndex = scrollY + row;
            if (lineIndex >= textEditor.lineCount()) break;

            String line = textEditor.getLine(lineIndex);

            for (int col = 0; col < TERM_COLUMNS; col++) {
                int charIndex = scrollX + col;
                if (charIndex >= line.length()) break;

                char c = line.charAt(charIndex);

                context.drawText(
                        textRenderer,
                        String.valueOf(c),
                        textX + col * charWidth,
                        textY + row * LINE_HEIGHT,
                        0xFFFFFF,
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

    private void drawCursor(DrawContext context, int textX, int textY, int maxLines, int maxVisibleLines) {
        if ((System.currentTimeMillis() / 500) % 2 != 0) return;

        if (textEditor.getCursorLine() < scrollY || textEditor.getCursorLine() >= scrollY + maxVisibleLines) return;
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

        switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT -> {
                textEditor.moveCursorHorizontal(-1);
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                textEditor.moveCursorHorizontal(1);
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                textEditor.moveCursorVertical(-1);
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
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
                textEditor.applyHome();
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_END -> {
                textEditor.applyEnd();
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_PAGE_UP -> {
                textEditor.moveCursorVertical(-TERM_ROWS);
                ensureCursorVisible();
                return true;
            }
            case GLFW.GLFW_KEY_PAGE_DOWN -> {
                textEditor.moveCursorVertical(TERM_ROWS);
                ensureCursorVisible();
                return true;
            }

        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

}
