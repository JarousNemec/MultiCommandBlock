package org.jardathedev.multicommandblock.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jardathedev.multicommandblock.entity.CommandProcessorBlockEntity;
import org.jardathedev.multicommandblock.registry.ModPackets;
import org.jardathedev.multicommandblock.screen.CommandProcessorScreen;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
                ModPackets.OPEN_COMMAND_PROCESSOR,
                (client, handler, buf, responseSender) -> {

                    BlockPos pos = buf.readBlockPos();
                    int count = buf.readInt();

                    List<String> lines = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        lines.add(buf.readString());
                    }

                    client.execute(() -> {
                        if (client.world == null) return;

                        BlockEntity be = client.world.getBlockEntity(pos);
                        if (be instanceof CommandProcessorBlockEntity cp) {

                            // INIT SYNC
                            cp.setLines(lines);

                            client.setScreen(
                                    new CommandProcessorScreen(cp)
                            );
                        }
                    });
                }
        );


        ClientPlayNetworking.registerGlobalReceiver(
                ModPackets.SYNC_LINES,
                (client, handler, buf, responseSender) -> {

                    BlockPos pos = buf.readBlockPos();
                    int count = buf.readInt();

                    List<String> lines = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        lines.add(buf.readString());
                    }

                    client.execute(() -> {
                        if (client.world == null) return;

                        BlockEntity be = client.world.getBlockEntity(pos);
                        if (be instanceof CommandProcessorBlockEntity cp) {
                            cp.setLines(lines);
                        }
                    });
                }
        );


    }
}
