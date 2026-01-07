package org.jardathedev.multicommandblock.entity.processorProgram;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jardathedev.multicommandblock.Multicommandblock;
import org.jardathedev.multicommandblock.shared.BlockEntityAttributes;
import org.jardathedev.multicommandblock.shared.CommandLine;
import org.jardathedev.multicommandblock.shared.ExecutionFrame;

import java.util.List;

import static org.jardathedev.multicommandblock.util.CommandUtil.parseArguments;

public class ProgramRuntime {

    private final ProgramData program;
    private final CmdSourceStore cmdSourceStore;

    public ProgramRuntime(ProgramData program, CmdSourceStore cmdSourceStore) {
        this.program = program;
        this.cmdSourceStore = cmdSourceStore;
    }

    public void start() {
        program.setExecuting(true);
        program.setExecutionPointer(0);
        program.setSleepTicks(0);
    }

    public void stop() {
        program.setExecuting(false);
    }

    public void programTick(BlockEntityAttributes attrs) {
        //escape if not running
        if (!program.isExecuting())
            return;


        var programLines = program.getProgramLines();
        var executionStack = program.getExecutionStack();

        // apply sleep
        if (program.getSleepTicks() > 0) {
            program.decreaseSleepTicksByOne();
            return;
        }

        // apply repeat
        while (!executionStack.isEmpty()) {
            ExecutionFrame frame = executionStack.peek();
            if (program.getExecutionPointer() > frame.endIndex) {
                frame.remainingRevolutions--;

                if (frame.remainingRevolutions > 0) {
                    program.setExecutionPointer(frame.startIndex);
                    break;
                } else {
                    executionStack.pop();
                }

            } else {
                break;
            }
        }

        CommandLine line = program.getPointerLine();

        //skip non executable lines
        while (program.getExecutionPointer() < program.getSize() + 1
                && !line.isExecutable()) {
            program.increaseExecutionPointerByOne();
            line = program.getPointerLine();
        }

        //execute one line
        executeLine(line, attrs);

        //increase line pointer
        program.increaseExecutionPointerByOne();

        //stop run if end of program
        if (program.getExecutionPointer() < 0 || program.getExecutionPointer() >= programLines.size()) {
            Multicommandblock.LOGGER.info("Execution ended on index: {}", program.getExecutionPointer() - 1);
            program.setExecuting(false);
            program.setExecutionPointer(0);
        }
    }

    private void executeLine(CommandLine line, BlockEntityAttributes attrs) {
        MinecraftServer server = attrs.world().getServer();

        String command = line.commandBody();
        if (line.isMinecraft())
            executeMinecraftCommand(cmdSourceStore.getSource(attrs), server.getCommandManager(), server, command);
        else if (line.isCustom())
            executeCustomCommand(command);

    }

    private void executeMinecraftCommand(ServerCommandSource source, CommandManager commandManager, MinecraftServer
            server, String command) {
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
        if (args.size() <= 1)
            return;
        String command = args.get(0);
        List<String> params = args.subList(1, args.size());

        if ("sleep".equals(command) && !params.isEmpty()) {
            executeSleep(params);
        } else if ("repeat".equals(command) && !params.isEmpty()) {
            executeRepeat();
        }
    }

    private void executeRepeat() {
        var executionStack = program.getExecutionStack();

        ExecutionFrame frame = null;
        for (ExecutionFrame f : program.getExecutionFrames()) {
            if (f.enterIndex == program.getExecutionPointer()) {
                frame = f.copy();
                break;
            }
        }
        if (frame == null) {
            return;
        }
        if (frame.revolutionsCount <= 0) {
            program.setExecutionPointer(frame.endIndex);
            return;
        }
        frame.remainingRevolutions = frame.revolutionsCount;
        executionStack.push(frame);
    }

    private void executeSleep(List<String> params) {
        var sleepTicks = Math.max(0, Integer.parseInt(params.get(0)));
        program.setSleepTicks(sleepTicks);
    }
}
