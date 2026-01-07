package org.jardathedev.multicommandblock.entity.processorProgram;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jardathedev.multicommandblock.shared.BlockEntityAttributes;

public class CmdSourceStore {
    private ServerCommandSource cachedSource;

    public ServerCommandSource getSource(BlockEntityAttributes attrs) {
        try {
            if (cachedSource == null || cachedSource.getWorld() != attrs.world()) {
                MinecraftServer server = attrs.world().getServer();
                cachedSource = new ServerCommandSource(
                        server,
                        Vec3d.ofCenter(attrs.pos()),
                        Vec2f.ZERO,
                        attrs.world(),
                        4,
                        "CommandProcessor",
                        Text.literal("CommandProcessor"),
                        server,
                        null
                );
            }
            return cachedSource;
        } catch (Exception e) {
            return null;
        }
    }
}
