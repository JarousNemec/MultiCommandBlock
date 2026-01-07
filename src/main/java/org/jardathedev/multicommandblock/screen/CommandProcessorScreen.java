package org.jardathedev.multicommandblock.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jardathedev.multicommandblock.registry.ModPackets;
import org.jardathedev.multicommandblock.screen.textEditor.TextEditor;
import org.jardathedev.multicommandblock.screen.textEditorWidget.TextEditorWidget;
import org.jardathedev.multicommandblock.screen.textEditorWidget.TextEditorWidgetProps;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CommandProcessorScreen extends Screen {

    private TextEditorWidget textEditorWidget;
    private TextEditor textEditor;
    private final TextEditorWidgetProps editorProps;
    private final BlockPos pos;

    public CommandProcessorScreen(BlockPos pos, List<String> initialLines, List<Integer> invalidLines) {
        super(Text.empty());
        this.pos = pos;
        editorProps = new TextEditorWidgetProps();
        editorProps.pos = pos;
        editorProps.initialLines = initialLines;
        editorProps.invalidLines = invalidLines;
        editorProps.termRows = TextEditorWidgetProps.DEFAULT_TERM_ROWS;
        editorProps.termColumns = TextEditorWidgetProps.DEFAULT_TERM_COLUMNS;
        editorProps.lineHeight = TextEditorWidgetProps.DEFAULT_LINE_HEIGHT;
        editorProps.padding = TextEditorWidgetProps.DEFAULT_PADDING;
    }

    @Override
    protected void init() {
        super.init();
        initDimensions();
        textEditorWidget = new TextEditorWidget(editorProps, textRenderer);
        textEditor = textEditorWidget.getTextEditor();
        addDrawableChild(textEditorWidget);
    }

    private void initDimensions() {
        editorProps.charWidth = textRenderer.getWidth("W");
        int digits = String.valueOf(editorProps.initialLines.size()).length();
        editorProps.lineNumberWidth = textRenderer.getWidth("0".repeat(digits));
        editorProps.width =
                editorProps.lineNumberWidth +
                        editorProps.termColumns * editorProps.charWidth +
                        editorProps.padding * 3;

        editorProps.height =
                editorProps.termRows * editorProps.lineHeight + editorProps.padding * 2;

        editorProps.x = (width - editorProps.width) / 2;
        editorProps.y = (height - editorProps.height) / 2;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);

        if (!handled) {
            setFocused(null);
        }

        return handled;
    }

    @Override
    public void close() {
        super.close();

        if (!textEditorWidget.isDirty()) return;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(this.pos);

        buf.writeInt(textEditor.getLinesCount());
        for (String line : textEditor.getLines()) {
            buf.writeString(line);
        }

        ClientPlayNetworking.send(ModPackets.SAVE_LINES, buf);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x88000000);
        super.render(context, mouseX, mouseY, delta);
    }
}
