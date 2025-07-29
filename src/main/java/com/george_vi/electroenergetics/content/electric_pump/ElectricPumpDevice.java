package com.george_vi.electroenergetics.content.electric_pump;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Map;

public class ElectricPumpDevice extends SimulatedDevice {
    public ElectricPumpDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        if (level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricPumpBlockEntity be)
                bridges.builder(pos).resistor(0, 1, be.getResistance());
    }

    @Override
    public void postTick(BlockPos pos, Level level, Map<Node, Double> voltages, Map<NodeConnection, Double> sourceAmps, CompoundTag extraData) {
        if (voltages.size() != 2)
            return;
        double vd = voltages.get(new Node(0, pos)) - voltages.get(new Node(1, pos));

        if (level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricPumpBlockEntity be)
                be.setVoltage((float) vd);
    }
}
