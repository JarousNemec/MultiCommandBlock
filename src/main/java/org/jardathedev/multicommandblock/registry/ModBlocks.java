package org.jardathedev.multicommandblock.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jardathedev.multicommandblock.Multicommandblock;
import org.jardathedev.multicommandblock.block.CommandProcessorBlock;

public class ModBlocks {
    public static CommandProcessorBlock COMMAND_PROCESSOR;

    public static void register() {
        COMMAND_PROCESSOR = Registry.register(Registries.BLOCK, Identifier.of(Multicommandblock.MOD_ID, "command_processor"), new CommandProcessorBlock(FabricBlockSettings.create()));
    }
}
