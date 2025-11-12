package com.george_vi.electroenergetics.content.electronic_components.diode;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.Map;

public class DiodeDevice extends SimulatedDevice {
    public DiodeDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        // Thanks, ChatGPT
        double voltage = extraData.getDouble("Voltage");

        double iS = 10e-12d;
        double vT = 0.046d;

        voltage = Mth.clamp(voltage, -0.8, 0.8);

        double Cj = 1e-12d;
        double gCap = Cj / bridges.getTimeStep();
        double iEqCap = gCap * voltage;

        double g = Math.max(1e-12d, (iS / vT) * Math.exp(voltage / vT)) + gCap;

        double resistance = 1 / g;
        double currentSource = iS * (Math.exp(voltage / vT) - 1) - g * voltage;

        bridges.builder(pos)
                .resistor(0, 1, resistance)
                .idealCurrentSource(0, 1, -currentSource - iEqCap);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        extraData.putDouble("Voltage", results.getVoltageAt(pos, 1, 0));
    }
}

