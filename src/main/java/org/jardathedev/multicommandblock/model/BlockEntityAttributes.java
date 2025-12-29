package org.jardathedev.multicommandblock.model;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public record BlockEntityAttributes (ServerWorld world, BlockPos pos) {
}
