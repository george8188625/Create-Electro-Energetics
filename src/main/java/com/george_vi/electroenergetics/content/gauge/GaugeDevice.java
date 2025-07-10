package com.george_vi.electroenergetics.content.gauge;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.Node;
import com.george_vi.electroenergetics.simulation.NodeConnection;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Map;

public class GaugeDevice extends SimulatedDevice {
    public final boolean voltmeter;
    public GaugeDevice(ResourceLocation id, boolean voltmeter) {
        super(id);
        this.voltmeter = voltmeter;
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        bridges.builder(pos).resistor(0, 1, voltmeter ? 1_000_000 : 0.01);
    }

    @Override
    public void postTick(BlockPos pos, Level level, Map<Node, Double> voltages, Map<NodeConnection, Double> sourceAmps, CompoundTag extraData) {
        if (!level.isLoaded(pos))
            return;

        if (voltages.size() != 2)
            return;
        double vd = Math.abs(voltages.get(new Node(0, pos)) - voltages.get(new Node(1, pos)));

        if (level.getBlockEntity(pos) instanceof ElectricGaugeBlockEntity be)
            be.setValue(voltmeter ? vd : vd / 0.1);
    }

    @Override
    public int sendVoltagesDistance() {
        return 80;
    }
}
