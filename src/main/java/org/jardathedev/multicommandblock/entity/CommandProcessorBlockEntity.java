package org.jardathedev.multicommandblock.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.math.BlockPos;
import org.jardathedev.multicommandblock.registry.ModBlockEntities;

import java.util.ArrayList;
import java.util.List;

public class CommandProcessorBlockEntity extends BlockEntity {

    private List<String> lines = new ArrayList<>();

    public CommandProcessorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMMAND_PROCESSOR_BLOCK_ENTITY, pos, state);
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> newLines) {
        this.lines = new ArrayList<>(newLines);
        markDirty();

        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
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
