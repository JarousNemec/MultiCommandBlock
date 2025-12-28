package org.jardathedev.multicommandblock.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jardathedev.multicommandblock.Multicommandblock;
import org.jardathedev.multicommandblock.entity.CommandProcessorBlockEntity;

public class ModBlockEntities {

    public static BlockEntityType<CommandProcessorBlockEntity> COMMAND_PROCESSOR_BLOCK_ENTITY;

    public static void register(){
        COMMAND_PROCESSOR_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(Multicommandblock.MOD_ID, "command_block_entity"), FabricBlockEntityTypeBuilder
                .create(
                        CommandProcessorBlockEntity::new,
                        ModBlocks.COMMAND_PROCESSOR
                )
                .build());
    }
}
