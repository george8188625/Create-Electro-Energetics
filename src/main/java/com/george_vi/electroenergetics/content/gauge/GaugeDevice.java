package com.george_vi.electroenergetics.content.gauge;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class GaugeDevice extends SimulatedDevice {
    public final boolean voltmeter;
    public GaugeDevice(ResourceLocation id, boolean voltmeter) {
        super(id);
        this.voltmeter = voltmeter;
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        bridges.builder(pos).resistor(0, 1, voltmeter ? 1_000_000 : 0.1);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        if (!level.isLoaded(pos))
            return;

        double vd = results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 1);

        if (level.getBlockEntity(pos) instanceof ElectricGaugeBlockEntity be) {
            be.voltage = vd;
            be.setValue(voltmeter ? Math.abs(vd) : Math.abs(vd) / 0.1);
        }
    }

    @Override
    public int sendVoltagesDistance() {
        return 80;
    }
}
