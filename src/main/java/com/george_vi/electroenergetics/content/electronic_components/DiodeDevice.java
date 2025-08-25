package com.george_vi.electroenergetics.content.electronic_components;

import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Map;

public class DiodeDevice extends SimulatedDevice {
    public DiodeDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        bridges.builder(pos)
                .resistor(0, 1, extraData.getBoolean("Forward") ? 9999 : 0.5);
    }

    @Override
    public void postTick(BlockPos pos, Level level, Map<Node, Double> voltages, Map<NodeConnection, Double> sourceAmps, CompoundTag extraData) {
        if (voltages.size() != 2)
            return;

        extraData.putBoolean("Forward", voltages.get(new Node(0, pos)) > voltages.get(new Node(1, pos)));
    }
}
