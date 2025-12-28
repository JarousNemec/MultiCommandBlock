package org.jardathedev.multicommandblock.registry;

import net.minecraft.util.Identifier;
import org.jardathedev.multicommandblock.Multicommandblock;

public class ModPackets {
    public static final Identifier SAVE_LINES =
            new Identifier(Multicommandblock.MOD_ID, "save_lines");

    public static final Identifier OPEN_COMMAND_PROCESSOR =
            new Identifier(Multicommandblock.MOD_ID, "open_command_processor");

    public static final Identifier SYNC_LINES =
            new Identifier(Multicommandblock.MOD_ID, "sync_lines");
}
