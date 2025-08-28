package com.george_vi.electroenergetics.content.voltage_regulator;

import com.george_vi.electroenergetics.content.transformer.TransformerBlockEntity;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.Map;

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
        Node p1 = new Node(0, pos), p2 = new Node(1, pos);
        Node s1 = new Node(2, pos), s2 = new Node(3, pos);

        double vPrimary = results.getVoltageAt(p1) - results.getVoltageAt(p2);
        if (Math.abs(vPrimary) < 1)
            vPrimary = 0;


        double storedEnergy = extraData.getDouble("StoredEnergy");

        double vLastTickHighestPrimary = vPrimary > 0 ?
                Math.max(Math.abs(extraData.getDouble("LastPrimaryVoltage")), Math.abs(vPrimary)) :
                - Math.max(Math.abs(extraData.getDouble("LastPrimaryVoltage")), Math.abs(vPrimary));

        double maxEnergy = 2_000_000;

        double current = results.getCurrentThrough(s1, s2);

        double load = Math.abs(current * (results.getVoltageAt(s1) - results.getVoltageAt(s2)));
        double primaryCurrent = vPrimary / (extraData.getDouble("PrimaryResistance") == 0 ? 10 : extraData.getDouble("PrimaryResistance"));
        double incomingEnergy = primaryCurrent * vPrimary;

        if (level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof VoltageRegulatorBlockEntity be)
                be.power = load;

        storedEnergy += incomingEnergy;
        if (load < 100 && storedEnergy > 100)
            load += 100;

        storedEnergy -= load;
        if ((storedEnergy - load) > maxEnergy)
            storedEnergy = Math.max(maxEnergy, storedEnergy / 1.5);

        double resistance;

        if ((storedEnergy - load) < maxEnergy) {
            double availableEnergy = maxEnergy - storedEnergy;
            double divisor = (availableEnergy > 1000 ? availableEnergy / 100 : availableEnergy) + load;
            resistance = Math.abs(vLastTickHighestPrimary / (divisor / vLastTickHighestPrimary));
        } else if (load > 10) {
            double divisor = (storedEnergy > maxEnergy ? load / 2.0 : load * 2);
            resistance = Math.abs(vLastTickHighestPrimary / (divisor / vLastTickHighestPrimary));
        } else {
            resistance = 999999;
        }

        float targetedVoltage = extraData.getFloat("Voltage");
        targetedVoltage = (float) Mth.clamp(targetedVoltage, Math.abs(vLastTickHighestPrimary) / 1.2, Math.abs(vLastTickHighestPrimary) * 1.2);

        double resultingVoltage = Math.abs(results.getVoltageAt(s1) - results.getVoltageAt(s2));
        double potentialVoltage = Math.abs(extraData.getFloat("Voltage"));

        if (resultingVoltage - potentialVoltage < -potentialVoltage / 500) {
            extraData.putInt("AdditionalSteps", Math.clamp(extraData.getInt("AdditionalSteps") + 1, -30, 30));
        } else if (resultingVoltage - potentialVoltage > potentialVoltage / 500) {
            extraData.putInt("AdditionalSteps", Math.clamp(extraData.getInt("AdditionalSteps") - 1, -30, 30));
        }

        targetedVoltage *= 1 + (extraData.getInt("AdditionalSteps") / 300f);


        extraData.putDouble("PrimaryResistance", storedEnergy / maxEnergy < 0.75 ? resistance / 10 : resistance);
        extraData.putDouble("StoredEnergy", storedEnergy);
        extraData.putDouble("SecondaryVoltage", storedEnergy > 100 ? targetedVoltage : 0);
        extraData.putDouble("LastPrimaryVoltage", vPrimary);
    }
}
