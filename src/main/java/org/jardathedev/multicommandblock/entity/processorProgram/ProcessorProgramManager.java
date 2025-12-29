package org.jardathedev.multicommandblock.entity.processorProgram;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jardathedev.multicommandblock.model.BlockEntityAttributes;
import org.jardathedev.multicommandblock.model.CommandLine;
import org.jardathedev.multicommandblock.model.LineState;
import org.jardathedev.multicommandblock.model.LineType;

import java.util.ArrayList;
import java.util.List;

import static org.jardathedev.multicommandblock.util.CommandUtil.*;

public class ProcessorProgramManager {
    private final List<CommandLine> _programLines;
    private final List<String> _uncompiledLines;
    private boolean _compiled = false;

    private int _executionIndex = 0;
    private int _sleepTicks = 0;
    private boolean _executing = false;

    private ServerCommandSource _cachedSource;

    public ProcessorProgramManager() {
        this._programLines = new ArrayList<>();
        this._uncompiledLines = new ArrayList<>();
    }

    public List<String> getRawLines() {
        List<String> lines = new ArrayList<>();
        for (CommandLine line : _programLines) {
            lines.add(line.raw());
        }
        return lines;
    }


    public List<Integer> getInvalidLines() {
        List<Integer> invalidLines = new ArrayList<>();
        for (int i = 0; i < _programLines.size(); i++) {
            if (!_programLines.get(i).isValid()) {
                invalidLines.add(i);
            }
        }
        return invalidLines;
    }

    public void setClientRawLines(List<String> newLines, List<Integer> invalidLines) {
        _programLines.clear();
        for (int i = 0; i < newLines.size(); i++) {
            String line = newLines.get(i);
            LineState lineState = invalidLines.contains(i) ? LineState.INVALID : LineState.VALID;
            _programLines.add(new CommandLine(line, LineType.RAW, lineState, false));
        }
    }

    public void setNewLines(List<String> uncompiledLines) {
        _uncompiledLines.clear();
        _uncompiledLines.addAll(uncompiledLines);
        _compiled = false;
    }

    public void start(BlockEntityAttributes blockEntityAttributes) {
        _executing = true;
        _executionIndex = 0;
        _sleepTicks = 0;
    }

    public void stop() {
        _executing = false;
    }

    public void programTick(BlockEntityAttributes attrs) {
        if (!_compiled) {
            compileLines(_uncompiledLines, attrs);
        }

        if (!_executing) return;

        // čekáme (sleep)
        if (_sleepTicks > 0) {
            _sleepTicks--;
            return;
        }

        // konec
        if (_executionIndex >= _programLines.size()) {
            _executing = false;
            _executionIndex = 0;
            return;
        }

        if (_programLines.get(_executionIndex).isExecutable()) {
            _executionIndex++;
            return;
        }

        executeProgramNextStep(attrs);
    }

    private void executeProgramNextStep(BlockEntityAttributes attrs) {
        MinecraftServer server = attrs.world().getServer();

        CommandLine line = _programLines.get(_executionIndex);
        _executionIndex++;

        if (line.raw().length() < 2) return;
        String command = line.commandBody();
        if (line.isMinecraft())
            executeMinecraftCommand(getSource(attrs), server.getCommandManager(), server, command);
        else if (line.isCustom())
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
            _sleepTicks = Math.max(0, Integer.parseInt(params.get(0)));
        }
    }

    private void compileLines(List<String> lines, BlockEntityAttributes attrs) {
        _programLines.clear();
        MinecraftServer server = attrs.world().getServer();

        for (String line : lines) {
            CommandLine programLine;
            if (line.isBlank()) {
                programLine = new CommandLine(line, LineType.BLANK, LineState.VALID, false);
            } else if (line.startsWith(COMMENT_START_CHAR)) {
                programLine = new CommandLine(line, LineType.COMMENT, LineState.VALID, false);
            } else if (line.length() < 2 || (!line.startsWith(MINECRAFT_COMMAND_START_CHAR) && !line.startsWith(CUSTOM_COMMAND_START_CHAR))) {
                programLine = new CommandLine(line, LineType.RAW, LineState.INVALID, false);
            } else if (line.startsWith(MINECRAFT_COMMAND_START_CHAR))
                if (!isValidMinecraftCommand(line.substring(1), getSource(attrs), server.getCommandManager())) {
                    programLine = new CommandLine(line, LineType.RAW, LineState.INVALID, false);
                } else {
                    programLine = new CommandLine(line, LineType.MINECRAFT, LineState.VALID, false);
                }
            else if (line.startsWith(CUSTOM_COMMAND_START_CHAR))
                if (!isValidCustomCommand(line.substring(1))) {
                    programLine = new CommandLine(line, LineType.RAW, LineState.INVALID, false);
                } else {
                    programLine = new CommandLine(line, LineType.CUSTOM, LineState.VALID, false);
                }
            else programLine = new CommandLine(line, LineType.RAW, LineState.INVALID, false);
            _programLines.add(programLine);
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

    private ServerCommandSource getSource(BlockEntityAttributes attrs) {
        if (_cachedSource == null || _cachedSource.getWorld() != attrs.world()) {
            MinecraftServer server = attrs.world().getServer();
            _cachedSource = new ServerCommandSource(
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
        return _cachedSource;
    }
}
