package org.jardathedev.multicommandblock;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jardathedev.multicommandblock.entity.CommandProcessorBlockEntity;
import org.jardathedev.multicommandblock.registry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Multicommandblock implements ModInitializer {

    public static final String MOD_ID = "multicommandblock";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();

        ServerPlayNetworking.registerGlobalReceiver(
                ModPackets.SAVE_LINES,
                (server, player, handler, buf, responseSender) -> {

                    BlockPos pos = buf.readBlockPos();

                    int count = buf.readInt();
                    List<String> lines = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        lines.add(buf.readString());
                    }

                    server.execute(() -> {
                        BlockEntity be = player.getWorld().getBlockEntity(pos);
                        if (be instanceof CommandProcessorBlockEntity cp) {

                            cp.setLinesServer(lines);

                            PacketByteBuf out = PacketByteBufs.create();
                            out.writeBlockPos(pos);

                            out.writeInt(lines.size());
                            for (String line : lines) {
                                out.writeString(line);
                            }

                            List<Integer> invalid = cp.getInvalidLines();
                            out.writeInt(invalid.size());
                            for (int i : invalid) {
                                out.writeInt(i);
                            }

                            ServerPlayNetworking.send(
                                    (ServerPlayerEntity) player,
                                    ModPackets.SYNC_LINES,
                                    out
                            );
                        }
                    });
                }
        );


    }
}
