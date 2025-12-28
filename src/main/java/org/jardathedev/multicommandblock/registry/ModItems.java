package org.jardathedev.multicommandblock.registry;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jardathedev.multicommandblock.Multicommandblock;

public class ModItems {

    public static BlockItem COMMAND_PROCESSOR_ITEM;

    public static void register() {
        COMMAND_PROCESSOR_ITEM = Registry.register(Registries.ITEM, Identifier.of(Multicommandblock.MOD_ID, "command_processor_item"), new BlockItem(ModBlocks.COMMAND_PROCESSOR, new FabricItemSettings()));
    }
}
