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
import org.jardathedev.multicommandblock.entity.processorProgram.ProcessorProgramManager;
import org.jardathedev.multicommandblock.model.BlockEntityAttributes;
import org.jardathedev.multicommandblock.registry.ModBlockEntities;

import java.util.ArrayList;
import java.util.List;

public class CommandProcessorBlockEntity extends BlockEntity {

    public final ProcessorProgramManager program;


    private boolean wasPowered = false;

    public CommandProcessorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMMAND_PROCESSOR_BLOCK_ENTITY, pos, state);
        program = new ProcessorProgramManager();
    }

    public List<String> getLines() {
        return program.getRawLines();
    }

    public List<Integer> getInvalidLines() {
        return program.getInvalidLines();
    }

    public void setLinesServer(List<String> newLines) {
        program.setNewLines(newLines);
        markDirty();
    }

    public void setLinesClient(List<String> newLines, List<Integer> invalid) {
        program.setClientRawLines(newLines, invalid);
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
        program.start(new BlockEntityAttributes(world, pos));
    }

    public void onRedstoneFall(ServerWorld world) {
        program.stop();
    }

    public static void tick(World world, BlockPos pos, BlockState state, CommandProcessorBlockEntity be) {
        if (world.isClient) return;
        be.program.programTick(new BlockEntityAttributes((ServerWorld) world, pos));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtList list = new NbtList();
        for (String line : program.getRawLines()) {
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
        program.setNewLines(lines);
    }
}
