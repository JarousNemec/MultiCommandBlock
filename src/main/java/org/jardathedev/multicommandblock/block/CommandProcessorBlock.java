package org.jardathedev.multicommandblock.block;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jardathedev.multicommandblock.Multicommandblock;
import org.jardathedev.multicommandblock.entity.CommandProcessorBlockEntity;
import org.jardathedev.multicommandblock.registry.ModPackets;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandProcessorBlock extends BlockWithEntity {
    public CommandProcessorBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {

        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);

            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof CommandProcessorBlockEntity cp) {

                PacketByteBuf out = PacketByteBufs.create();
                out.writeBlockPos(pos);

                List<String> lines = cp.getLines();
                out.writeInt(lines.size());
                for (String line : lines) {
                    out.writeString(line);
                }

                ServerPlayNetworking.send(
                        serverPlayer,
                        ModPackets.OPEN_COMMAND_PROCESSOR,
                        out
                );
            }
        }

        return ActionResult.SUCCESS;
    }


    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CommandProcessorBlockEntity(pos, state);
    }
}
