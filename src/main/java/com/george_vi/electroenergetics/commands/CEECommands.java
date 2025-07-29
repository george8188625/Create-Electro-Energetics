package com.george_vi.electroenergetics.commands;

import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.sun.jdi.connect.Connector;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
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
        source.sendSuccess(() -> Component.literal("Networks: " + SimulationTicker.performances.size()).withStyle(blue), false);
        for (SimulationTicker.SimulationPerformance performance : List.copyOf(SimulationTicker.performances).stream().sorted((Comparator.comparingInt(SimulationTicker.SimulationPerformance::totalTime))).toList()) {
            if (performance.nodes() == 1)
                continue;
            source.sendSuccess(() -> Component.literal("Network of Voltage " + performance.minVoltage() + " - " + performance.maxVoltage() + " N: " + performance.nodes() + " ON: " + performance.optimizedNodes() + " OT: " + performance.optimizationTime() + "μs ST: " + performance.solutionTime() + "μs TT: " + performance.totalTime() + "μs"), false);
        }
        source.sendSuccess(() -> Component.literal("Whole simulation: " + SimulationTicker.totalTime + "μs").withStyle(darkBlue), false);
        source.sendSuccess(() -> Component.literal("-+--------------------------------+-"), false);
        return 1;
    }

    public static int deviceData(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");
        InfrastructureSavedData.SimulatedDeviceInstance deviceInstance = InfrastructureSavedData.load(source.getLevel()).getDevice(pos);
        if (deviceInstance == null)
            throw NO_DEVICE_ERROR.create();
        source.sendSuccess(() -> Component.literal("The device's stored data is: ").append(NbtUtils.toPrettyComponent(deviceInstance.extraData())), false);
        return 1;
    }

    public static int devices(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        BlockPos pos = BlockPos.containing(source.getPosition());
        int distance = IntegerArgumentType.getInteger(ctx, "distance");

        for (InfrastructureSavedData.SimulatedDeviceInstance instance : InfrastructureSavedData.load(source.getLevel()).getDevices())
            if (Math.sqrt(instance.pos().distSqr(pos)) <= distance)
                source.sendSuccess(() -> Component.literal(instance.simulatedDevice().getID().toString() + " : " + instance.pos().toShortString()).withStyle(blue), false);

        return 1;
    }
}
