package org.jardathedev.multicommandblock.screen.textEditorWidget;

import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class TextEditorWidgetProps {

    public static final int DEFAULT_TERM_COLUMNS = 64;
    public static final int DEFAULT_TERM_ROWS = 20;
    public static final int DEFAULT_LINE_HEIGHT = 10;
    public static final int DEFAULT_PADDING = 6;

    public int x;
    public int y;
    public int width;
    public int height;
    public Text message;
    public BlockPos pos;
    public List<String> initialLines;
    public List<Integer> invalidLines;
    public int lineNumberWidth;
    public int charWidth;
    public int termColumns;
    public int termRows;
    public int lineHeight;
    public int padding;

    public TextEditorWidgetProps(){
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
        this.lineNumberWidth = 0;
        this.charWidth = 0;
        this.termRows = DEFAULT_TERM_ROWS;
        this.termColumns = DEFAULT_TERM_COLUMNS;
        this.lineHeight = DEFAULT_LINE_HEIGHT;
        this.padding = DEFAULT_PADDING;
        message = Text.empty();
        pos = new BlockPos(0, 0, 0);
        initialLines = new ArrayList<>();
        invalidLines = new ArrayList<>();
    }

}
