package org.jardathedev.multicommandblock.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jardathedev.multicommandblock.registry.ModBlockEntities;

import java.util.ArrayList;
import java.util.List;

import static org.jardathedev.multicommandblock.util.commandUtil.isInteger;
import static org.jardathedev.multicommandblock.util.commandUtil.parseArguments;


public class CommandProcessorBlockEntity extends BlockEntity {

    private List<String> lines = new ArrayList<>();
    private List<Integer> invalidLines = new ArrayList<>();
    private final List<Integer> minecraftCommandLines = new ArrayList<>();
    private final List<Integer> customCommandLines = new ArrayList<>();

    private boolean wasPowered = false;
    private int executionIndex = 0;
    private int sleepTicks = 0;
    private boolean executing = false;

    public static final String COMMENT_START_CHAR = "#";
    public static final String CUSTOM_COMMAND_START_CHAR = "%";
    public static final String MINECRAFT_COMMAND_START_CHAR = "/";

    private ServerCommandSource cachedSource;

    public CommandProcessorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMMAND_PROCESSOR_BLOCK_ENTITY, pos, state);
    }

    public List<String> getLines() {
        return lines;
    }

    public List<Integer> getInvalidLines() {
        return invalidLines;
    }

    public void setLinesServer(List<String> newLines, ServerWorld world) {

        this.lines = new ArrayList<>(newLines);
        validateLines(world);
        markDirty();
    }

    public void setLinesClient(List<String> newLines, List<Integer> invalid) {
        this.lines = new ArrayList<>(newLines);
        this.invalidLines = new ArrayList<>(invalid);
    }

    public void neighbourUpdate(BlockPos pos, ServerWorld world) {
        boolean powered = world.isReceivingRedstonePower(pos);

        // rising edge
        if (powered && !wasPowered) {
            onRedstoneRise(world);
        }

        // falling edge
        if (!powered && wasPowered) {
            onRedstoneFall(world);
        }

        wasPowered = powered;
    }

    public void onRedstoneRise(ServerWorld world) {
        executing = true;
        executionIndex = 0;
        sleepTicks = 0;
    }

    public void onRedstoneFall(ServerWorld world) {
    }

    public static void tick(World world, BlockPos pos, BlockState state, CommandProcessorBlockEntity be) {
        if (world.isClient) return;
        be.tickServer((ServerWorld) world);
    }

    private void tickServer(ServerWorld world) {
        if (!executing) return;

        // čekáme (sleep)
        if (sleepTicks > 0) {
            sleepTicks--;
            return;
        }

        // konec
        if (executionIndex >= lines.size()) {
            executing = false;
            executionIndex = 0;
            return;
        }

        if (invalidLines.contains(executionIndex) || lines.get(executionIndex).isBlank() || lines.get(executionIndex).startsWith(COMMENT_START_CHAR)) {
            executionIndex++;
            return;
        }

        executeProgramNextStep(world);
    }


    private void executeProgramNextStep(ServerWorld world) {
        MinecraftServer server = world.getServer();

        String line = lines.get(executionIndex);
        int index = executionIndex;
        executionIndex++;

        if (line.length() < 2) return;
        String command = line.substring(1);
        if (minecraftCommandLines.contains(index))
            executeMinecraftCommand(getSource(world), server.getCommandManager(), server, command);
        else if (customCommandLines.contains(index))
            executeCustomCommand(command);

    }

    private void executeMinecraftCommand(ServerCommandSource source, CommandManager commandManager, MinecraftServer server, String command) {
        try {
            commandManager.executeWithPrefix(source.withSilent(), command);
        } catch (Exception e) {
            server.sendMessage(
                    Text.literal("§cCommandProcessor error: " + e.getMessage())
            );
        }
    }

    private void executeCustomCommand(String arguments) {
        List<String> args = parseArguments(arguments);
        String command = args.get(0);
        List<String> params = args.subList(1, args.size());

        if ("sleep".equals(command) && !params.isEmpty()) {
            sleepTicks = Math.max(0, Integer.parseInt(params.get(0)));
        }
    }

    private void validateLines(ServerWorld world) {
        invalidLines.clear();
        minecraftCommandLines.clear();
        customCommandLines.clear();

        MinecraftServer server = world.getServer();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.isBlank() || line.startsWith(COMMENT_START_CHAR))
                continue;

            if (line.length() < 2 || (!line.startsWith(MINECRAFT_COMMAND_START_CHAR) && !line.startsWith(CUSTOM_COMMAND_START_CHAR))) {
                invalidLines.add(i);
                continue;
            }
            if (line.startsWith(MINECRAFT_COMMAND_START_CHAR))
                if (!isValidMinecraftCommand(line.substring(1), getSource(world), server.getCommandManager())) {
                    invalidLines.add(i);
                } else {
                    minecraftCommandLines.add(i);
                }
            else if (line.startsWith(CUSTOM_COMMAND_START_CHAR))
                if (!isValidCustomCommand(line.substring(1))) {
                    invalidLines.add(i);
                } else {
                    customCommandLines.add(i);
                }
        }
    }

    private boolean isValidMinecraftCommand(
            String command,
            ServerCommandSource source,
            CommandManager manager
    ) {
        var dispatcher = manager.getDispatcher();
        var parse = dispatcher.parse(command, source);

        if (parse.getContext().getNodes().isEmpty()) {
            return false;
        }

        if (parse.getReader().canRead()) {
            return false;
        }

        return true;
    }

    private boolean isValidCustomCommand(String line) {
        List<String> args = parseArguments(line);
        String command = args.get(0);
        List<String> params = args.subList(1, args.size());
        if ("sleep".equals(command) && !params.isEmpty()) {
            if (!isInteger(params.get(0)))
                return false;

            int ticks;
            try {
                ticks = Integer.parseInt(params.get(0));
            } catch (NumberFormatException e) {
                return false;
            }

            return ticks >= 0;
        }

        return false;
    }

    private ServerCommandSource getSource(ServerWorld world) {
        if (cachedSource == null || cachedSource.getWorld() != world) {
            MinecraftServer server = world.getServer();
            cachedSource = new ServerCommandSource(
                    server,
                    Vec3d.ofCenter(pos),
                    Vec2f.ZERO,
                    world,
                    4,
                    "CommandProcessor",
                    Text.literal("CommandProcessor"),
                    server,
                    null
            );
        }
        return cachedSource;
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);

        if (world instanceof ServerWorld serverWorld) {
            validateLines(serverWorld);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtList list = new NbtList();
        for (String line : lines) {
            list.add(NbtString.of(line));
        }
        nbt.put("Lines", list);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        lines.clear();
        NbtList list = nbt.getList("Lines", NbtElement.STRING_TYPE);
        for (int i = 0; i < list.size(); i++) {
            lines.add(list.getString(i));
        }
    }
}
