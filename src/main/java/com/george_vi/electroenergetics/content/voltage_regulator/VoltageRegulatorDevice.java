package com.george_vi.electroenergetics.content.voltage_regulator;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.Arrays;

public class VoltageRegulatorDevice extends SimulatedDevice {
    public VoltageRegulatorDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {

        bridges.builder(pos)
                .resistor(0, 1, extraData.getDouble("PrimaryResistance") == 0 ? 9999999 : extraData.getDouble("PrimaryResistance"))
                .energyLimitedSource(3, 2, extraData.getDouble("StoredEnergy"), extraData.getDouble("SecondaryVoltage"));
    }
    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        double storedEnergy = extraData.contains("StoredEnergy") ? extraData.getDouble("StoredEnergy") : 2_000_000;
        float ratio = extraData.getFloat("Ratio");
        if (ratio == 0)
            ratio = 1;

        double primaryVoltage = results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 1);
        double secondaryVoltage = results.getVoltageAt(pos, 3) - results.getVoltageAt(pos, 2);
        if (Math.abs(primaryVoltage) < 0.1)
            primaryVoltage = 0;

        // incoming energy and load

        double secondaryCurrent = results.getCurrentThrough(pos, 2, 3);
        double load = secondaryCurrent * secondaryVoltage;
        if (load < 0)
            load = 0;

        double primaryCurrent = results.getCurrentThrough(pos, 0, 1);
        double incomingEnergy = primaryCurrent * primaryVoltage;

        storedEnergy -= load;
        storedEnergy += incomingEnergy;
        if (storedEnergy < 0)
            storedEnergy = 0;

        // calculate the average primary voltage

        double[] prevVoltages = extraData.getList("PrevVoltages", Tag.TAG_DOUBLE).stream().mapToDouble(t -> ((DoubleTag)t).getAsDouble()).toArray();
        ListTag tag = new ListTag();
        tag.add(DoubleTag.valueOf(primaryVoltage));
        for (int i = 0; i < Math.min(12, prevVoltages.length); i++) {
            tag.add(DoubleTag.valueOf(prevVoltages[i]));
        }
        extraData.put("PrevVoltages", tag);

        double averageVoltage = (Arrays.stream(prevVoltages).sum() + primaryVoltage) / (prevVoltages.length + 1);

        double primaryResistance = (averageVoltage / ((load + 100) / averageVoltage));
        if (primaryResistance < 0.1)
            primaryResistance = 0.1;

        float targetedVoltage = extraData.getFloat("Voltage");
        targetedVoltage = (float) Mth.clamp(targetedVoltage, Math.abs(averageVoltage) / 1.2, Math.abs(averageVoltage) * 1.2);

        double resultingVoltage = Math.abs(secondaryVoltage);
        double potentialVoltage = Math.abs(extraData.getFloat("Voltage"));

        if (resultingVoltage - potentialVoltage < -potentialVoltage / 500) {
            extraData.putInt("AdditionalSteps", Math.clamp(extraData.getInt("AdditionalSteps") + 1, -30, 30));
        } else if (resultingVoltage - potentialVoltage > potentialVoltage / 500) {
            extraData.putInt("AdditionalSteps", Math.clamp(extraData.getInt("AdditionalSteps") - 1, -30, 30));
        }

        targetedVoltage *= 1 + (extraData.getInt("AdditionalSteps") / 300f);

        extraData.putDouble("PrimaryResistance", primaryResistance);
        extraData.putDouble("StoredEnergy", storedEnergy);
        extraData.putDouble("SecondaryVoltage", targetedVoltage);

        if (level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof VoltageRegulatorBlockEntity be)
                be.power = load;
    }
}
