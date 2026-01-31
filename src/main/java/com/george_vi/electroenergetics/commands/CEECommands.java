package com.george_vi.electroenergetics.commands;

import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;
import com.george_vi.electroenergetics.simulation.simulator.SimulationStats;
import com.george_vi.electroenergetics.simulation.util.SimulatorProfiler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.Level;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public class CEECommands {
    private static final UnaryOperator<Style> white = st -> st.withColor(ChatFormatting.WHITE.getColor());
    private static final UnaryOperator<Style> blue = st -> st.withColor(0xaac8e0);
    private static final UnaryOperator<Style> darkBlue = st -> st.withColor(0x88a5b7);
    private static final UnaryOperator<Style> darkerBlue = st -> st.withColor(0x6b8694);
    private static final UnaryOperator<Style> darkestBlue = st -> st.withColor(0x536b75);
    private static final UnaryOperator<Style> bright = st -> st.withColor(0xFFEFEF);
    private static final UnaryOperator<Style> orange = st -> st.withColor(0xFFAD60);

    public static final SimpleCommandExceptionType NO_DEVICE_ERROR = new SimpleCommandExceptionType(Component.literal("No devices found at this position."));
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("cee")
            .requires(cs -> cs.hasPermission(0))
            .then(
                Commands.literal("performance")
                      .requires(cs -> cs.hasPermission(2))
                      .executes(CEECommands::performance)
            ).then(
                Commands.literal("stats")
                        .requires(cs -> cs.hasPermission(2))
                        .executes(CEECommands::stats)
            ).then(
                Commands.literal("device_data")
                        .requires(cs -> cs.hasPermission(2))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(CEECommands::deviceData))
            ).then(
                Commands.literal("devices")
                        .requires(cs -> cs.hasPermission(2))
                        .then(Commands.argument("distance", IntegerArgumentType.integer(1))
                            .executes(CEECommands::devices))
            );

        dispatcher.register(root);
    }

    public static int performance(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.literal("-+------<< Simulation Performance: >>------+-"), false);
        List<SimulatorProfiler.ResultEntry> results = List.copyOf(SimulationTicker.profiler.getResults());
        long totalTime = results.stream().mapToLong(re -> re.timeTookNanos).sum();
        listResults(1, results, source, totalTime == 0 ? 1 : totalTime, totalTime);
        source.sendSuccess(() -> Component.literal("profiler: " + SimulationTicker.profiler.getProfilerOverheadNanos() / 1000 + " μs").withStyle(blue), false);
        source.sendSuccess(() -> Component.literal("threadedSimulation: " + SimulationTicker.profiler.getThreadedNanos() / 1000 + " μs").withStyle(blue), false);
        source.sendSuccess(() -> Component.literal("-+------------------------------------+-"), false);
        return 1;
    }

    private static void listResults(int depth, List<SimulatorProfiler.ResultEntry> entries, CommandSourceStack source, long totalTime, long parentTime) {
        if (entries == null)
            return;
        for (SimulatorProfiler.ResultEntry entry : entries) {
            float percentage = (float) entry.timeTookNanos / totalTime;
            float parentPercentage = (float) entry.timeTookNanos / parentTime;
            source.sendSuccess(() -> Component.literal("|  ".repeat(Math.max(0, depth - 1)) + "⊢ ").withStyle(blue)
                            .append(Component.literal(entry.id.toString() + " ").withStyle(bright))
                            .append(Component.literal(String.valueOf((entry.timeTookNanos / 1000))).append(" μs ").append(Component.literal(String.format("%.2f", percentage * 100) + " % ").withStyle(st -> st.withColor(Color.getHSBColor((float) (Math.pow(1 - percentage, 3) * 0.32f), 0.6f, 1f).getRGB()))))
                            .append(Component.literal(String.format("%.2f", parentPercentage * 100) + " %").withStyle(st -> st.withColor(Color.getHSBColor((float) (Math.pow(1 - parentPercentage, 3) * 0.32f), 0.6f, 1f).getRGB())))
                    , false);
            listResults(depth + 1, entry.children, source, totalTime, entry.timeTookNanos);
        }
    }

    public static int stats(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.literal("-+------<< Simulation Stats >>-------------+-"), false);
        for (Map.Entry<Level, SimulationStats> entry : SimulationTicker.allStats.entrySet()) {
            Level level = entry.getKey();
            SimulationStats stats = entry.getValue();
            source.sendSuccess(() -> Component.literal("totalNodes: ").withStyle(blue).append(Component.literal(String.valueOf(stats.totalNodes)).withStyle(orange)), false);
            Arrays.sort(stats.totalSeparatedNodes);
            Arrays.sort(stats.totalOptimizedNodes);
            source.sendSuccess(() -> Component.literal("perNetwork:").withStyle(blue), false);
            for (int i = stats.totalSeparatedNodes.length - 1; i >= 0; i--) {
                if (stats.totalSeparatedNodes.length - 1 - i >= 4) {
                    source.sendSuccess(() -> Component.literal("  ...").withStyle(blue), false);
                    break;
                }

                int finalI = i;
                source.sendSuccess(() -> Component.literal(" ⊢ ").withStyle(blue).append(Component.literal(String.valueOf(stats.totalSeparatedNodes[finalI])).withStyle(orange)), false);
            }

            source.sendSuccess(() -> Component.literal("optimized:").withStyle(blue), false);
            for (int i = stats.totalOptimizedNodes.length - 1; i >= 0; i--) {
                if (stats.totalOptimizedNodes.length - 1 - i >= 4) {
                    source.sendSuccess(() -> Component.literal("  ...").withStyle(blue), false);
                    break;
                }

                int finalI = i;
                source.sendSuccess(() -> Component.literal(" ⊢ ").withStyle(blue).append(Component.literal(String.valueOf(stats.totalOptimizedNodes[finalI])).withStyle(orange)), false);
            }
            source.sendSuccess(() -> Component.literal("totalDevices: ").withStyle(blue).append(Component.literal(String.valueOf(stats.totalDevices)).withStyle(orange)), false);
            source.sendSuccess(() -> Component.literal("totalMicroTickers: ").withStyle(blue).append(Component.literal(String.valueOf(stats.totalMicroTickers)).withStyle(orange)), false);

        }

        source.sendSuccess(() -> Component.literal("-+------------------------------------+-"), false);
        return 1;
    }

    public static int deviceData(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");
        SimulatedDeviceInstance deviceInstance = InfrastructureSavedData.load(source.getLevel()).getDevice(pos);
        if (deviceInstance == null)
            throw NO_DEVICE_ERROR.create();
        source.sendSuccess(() -> Component.literal("The device's stored data is: ").append(NbtUtils.toPrettyComponent(deviceInstance.write())), false);
        return 1;
    }

    public static int devices(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        BlockPos pos = BlockPos.containing(source.getPosition());
        int distance = IntegerArgumentType.getInteger(ctx, "distance");

        for (SimulatedDeviceInstance instance : InfrastructureSavedData.load(source.getLevel()).getDevices())
            if (Math.sqrt(instance.pos().distSqr(pos)) <= distance)
                source.sendSuccess(() -> Component.literal(instance.simulatedDevice().getID().toString() + " : " + instance.pos().toShortString()).withStyle(blue), false);

        return 1;
    }
}
