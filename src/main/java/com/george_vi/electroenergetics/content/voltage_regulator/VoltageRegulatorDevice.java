package com.george_vi.electroenergetics.content.voltage_regulator;

import com.george_vi.electroenergetics.content.transformer.TransformerBehaviour;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class VoltageRegulatorDevice extends SimulatedDevice<VoltageRegulatorDevice.DataHolder> {
    public VoltageRegulatorDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        double ratio = extraData.ratio;
        if (ratio == 0)
            ratio = 1;

        TransformerBehaviour.preTick(TransformerBehaviour.setupStandardNodes(pos), ratio, pos, bridges, extraData.transformerData);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        double power = TransformerBehaviour.postTick(TransformerBehaviour.setupStandardNodes(pos), results, extraData.transformerData);
        double secondaryVoltage = results.getVoltageAt(pos, 2, 3);

        if (extraData.be == null)
            if (level.getBlockEntity(pos) instanceof VoltageRegulatorBlockEntity be)
                extraData.be = be;

        if (extraData.be != null) {
            if (extraData.be.isRemoved())
                extraData.be = null;
            else {
                extraData.be.power = Math.abs(power);
            }
        }

        double targetedVoltage = extraData.voltage;
        double resultingVoltage = Math.abs(secondaryVoltage);

        if (resultingVoltage - targetedVoltage < -targetedVoltage / 500) {
            extraData.steps = Math.clamp(extraData.steps + 1, -60, 60);
        } else if (resultingVoltage - targetedVoltage > targetedVoltage / 500) {
            extraData.steps = Math.clamp(extraData.steps - 1, -60, 60);
        }

        extraData.ratio = 1 - (extraData.steps / 300f);
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.steps = tag.getInt("Steps");
        dataHolder.temp = tag.getFloat("Temp");
        dataHolder.ratio = tag.getDouble("Ratio");
        dataHolder.voltage = tag.getDouble("Voltage");
        dataHolder.transformerData = new TransformerBehaviour.TransformerBehaviourDataHolder(tag.getCompound("TransformerData"));
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Steps", extraData.steps);
        tag.putFloat("Temp", extraData.temp);
        tag.putDouble("Ratio", extraData.ratio);
        tag.putDouble("Voltage", extraData.voltage);
        tag.put("TransformerData", extraData.transformerData.write());
        return tag;
    }

    public static class DataHolder {
        public TransformerBehaviour.TransformerBehaviourDataHolder transformerData;
        public float temp;
        public double ratio;
        public VoltageRegulatorBlockEntity be;
        public int steps;
        public double voltage;
    }
}
