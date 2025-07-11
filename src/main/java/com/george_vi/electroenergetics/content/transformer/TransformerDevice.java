package com.george_vi.electroenergetics.content.transformer;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.Node;
import com.george_vi.electroenergetics.simulation.NodeConnection;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.Map;

public class TransformerDevice extends SimulatedDevice {
    public TransformerDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {

        bridges.builder(pos)
                .resistor(0, 1, extraData.getDouble("PrimaryResistance") == 0 ? 10 : extraData.getDouble("PrimaryResistance"))
                .energyLimitedSource(3, 2, extraData.getDouble("StoredEnergy"), extraData.getDouble("SecondaryVoltage"));
    }
    @Override
    public void postTick(BlockPos pos, Level level, Map<Node, Double> voltages, Map<NodeConnection, Double> sourceAmps, CompoundTag extraData) {
        if (voltages.size() < 4) return;

        Node p1 = new Node(0, pos), p2 = new Node(1, pos);
        Node s1 = new Node(2, pos), s2 = new Node(3, pos);

        double vPrimary = voltages.get(p1) - voltages.get(p2);
        if (Math.abs(vPrimary) < 0.1)
            vPrimary = 0;


        double storedEnergy = extraData.getDouble("StoredEnergy");
        float ratio = extraData.getFloat("Ratio");
        if (ratio == 0)
            ratio = 1;
        double vLastTickHighestPrimary = vPrimary > 0 ?
                Math.max(Math.abs(extraData.getDouble("LastPrimaryVoltage")), Math.abs(vPrimary)) :
                - Math.max(Math.abs(extraData.getDouble("LastPrimaryVoltage")), Math.abs(vPrimary));

        double maxEnergy = 1_00 * Math.abs(vLastTickHighestPrimary / ratio) + 1;

        double current = 0;
        for (Double d : sourceAmps.values())
            current = d;

        double load = Math.abs(current * (voltages.get(s1) - voltages.get(s2)));
        double primaryCurrent = vPrimary / (extraData.getDouble("PrimaryResistance") == 0 ? 10 : extraData.getDouble("PrimaryResistance"));
        double incomingEnergy = primaryCurrent * vPrimary;

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
            double divisor = (storedEnergy > maxEnergy ? load / 3.0 : load);
            resistance = Math.abs(vLastTickHighestPrimary / (divisor / vLastTickHighestPrimary));
        } else {
            resistance = 999999;
        }

        extraData.putDouble("PrimaryResistance", storedEnergy / maxEnergy < 0.75 ? resistance / 10 : resistance);
        extraData.putDouble("StoredEnergy", storedEnergy);
        extraData.putDouble("SecondaryVoltage", storedEnergy > 100 ? storedEnergy / 1_00 : 0);
        extraData.putDouble("LastPrimaryVoltage", vPrimary);
    }
}
