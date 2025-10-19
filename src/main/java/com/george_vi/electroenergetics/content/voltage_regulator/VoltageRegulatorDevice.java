package com.george_vi.electroenergetics.content.voltage_regulator;

import com.george_vi.electroenergetics.content.transformer.TransformerBehaviour;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class VoltageRegulatorDevice extends SimulatedDevice {
    public VoltageRegulatorDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        double ratio = extraData.getDouble("Ratio");
        if (ratio == 0)
            ratio = 1;

        TransformerBehaviour.preTick(TransformerBehaviour.setupStandardNodes(pos), ratio, pos, bridges, extraData);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        double power = TransformerBehaviour.postTick(TransformerBehaviour.setupStandardNodes(pos), results, extraData);
        double secondaryVoltage = results.getVoltageAt(pos, 2, 3);

        if (level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof VoltageRegulatorBlockEntity be)
                be.power = Math.abs(power);

        float targetedVoltage = extraData.getFloat("Voltage");
        double resultingVoltage = Math.abs(secondaryVoltage);

        if (resultingVoltage - targetedVoltage < -targetedVoltage / 500) {
            extraData.putInt("Steps", Math.clamp(extraData.getInt("Steps") + 1, -60, 60));
        } else if (resultingVoltage - targetedVoltage > targetedVoltage / 500) {
            extraData.putInt("Steps", Math.clamp(extraData.getInt("Steps") - 1, -60, 60));
        }

        extraData.putDouble("Ratio", 1 - (extraData.getInt("Steps") / 300f));
    }
}
