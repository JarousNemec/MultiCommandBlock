package org.jardathedev.multicommandblock.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jardathedev.multicommandblock.entity.processorProgram.ProgramManager;
import org.jardathedev.multicommandblock.shared.BlockEntityAttributes;
import org.jardathedev.multicommandblock.registry.ModBlockEntities;

import java.util.ArrayList;
import java.util.List;

public class CommandProcessorBlockEntity extends BlockEntity {

    public final ProgramManager programManager;


    private boolean wasPowered = false;

    public CommandProcessorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMMAND_PROCESSOR_BLOCK_ENTITY, pos, state);
        programManager = new ProgramManager();
    }

    public List<String> getLines() {
        return programManager.getRawLines();
    }

    public List<Integer> getInvalidLines() {
        return programManager.getInvalidLines();
    }

    public void setLinesServer(List<String> newLines) {
        programManager.setNewLines(newLines);
        markDirty();
    }

    public void setLinesClient(List<String> newLines, List<Integer> invalid) {
        programManager.setClientRawLines(newLines, invalid);
    }

    public void neighbourUpdate(BlockPos pos, ServerWorld world) {
        boolean isPowered = world.isReceivingRedstonePower(pos);

        // rising edge
        if (isPowered && !wasPowered) {
            onRedstoneRise(world);
        }

        // falling edge
        if (!isPowered && wasPowered) {
            onRedstoneFall(world);
        }

        wasPowered = isPowered;
    }

    public void onRedstoneRise(ServerWorld world) {
        programManager.start();
    }

    public void onRedstoneFall(ServerWorld world) {
        programManager.stop();
    }

    public static void tick(World world, BlockPos pos, BlockState state, CommandProcessorBlockEntity be) {
        if (world.isClient) return;
        be.programManager.programTick(new BlockEntityAttributes((ServerWorld) world, pos));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtList list = new NbtList();
        for (String line : programManager.getRawLines()) {
            list.add(NbtString.of(line));
        }
        nbt.put("Lines", list);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        NbtList list = nbt.getList("Lines", NbtElement.STRING_TYPE);
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            lines.add(list.getString(i));
        }
        programManager.setNewLines(lines);
    }
}
