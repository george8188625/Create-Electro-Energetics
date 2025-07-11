package com.george_vi.electroenergetics.content.voltage_regulator;

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

public class VoltageRegulatorDevice extends SimulatedDevice {
    public VoltageRegulatorDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {

        bridges.builder(pos)
                        .resistor(0, 1, extraData.getDouble("PrimaryResistance") == 0 ? 10 : extraData.getDouble("PrimaryResistance"))
                        .idealVoltageSource(3, 2, extraData.getDouble("SecondaryVoltage"));
    }
    @Override
    public void postTick(BlockPos pos, Level level, Map<Node, Double> voltages, Map<NodeConnection, Double> sourceAmps, CompoundTag extraData) {
        float targetedVoltage = extraData.getFloat("Voltage");
        if (voltages.size() < 4) return;

        Node p1 = new Node(0, pos), p2 = new Node(1, pos);
        Node s1 = new Node(2, pos), s2 = new Node(3, pos);

        double vPrimary = voltages.get(p1) - voltages.get(p2);
        if (Math.abs(vPrimary) < 0.1)
            vPrimary = 0;


        double storedEnergy = extraData.getDouble("StoredEnergy");

        double vLastTickHighestPrimary = vPrimary > 0 ?
                Math.max(Math.abs(extraData.getDouble("LastPrimaryVoltage")), Math.abs(vPrimary)) :
                -Math.max(Math.abs(extraData.getDouble("LastPrimaryVoltage")), Math.abs(vPrimary));

        double maxEnergy = 1_000 * Math.abs(vLastTickHighestPrimary);

        double load = Math.abs(sourceAmps.getOrDefault(new NodeConnection(s1, s2), 0d) * (voltages.get(s1) - voltages.get(s2)));
        double primaryCurrent = vPrimary / (extraData.getDouble("PrimaryResistance") == 0 ? 10 : extraData.getDouble("PrimaryResistance"));
        double incomingEnergy = primaryCurrent * vPrimary;

        storedEnergy += incomingEnergy;
        if (load < 100 && storedEnergy > 100)
            load += 100;
        storedEnergy -= load;

//        if (storedEnergy > 0)
//            storedEnergy /= 1.02;

        extraData.putDouble("PrimaryResistance", storedEnergy < maxEnergy ? Math.abs(vLastTickHighestPrimary / (((maxEnergy - storedEnergy > 1000 ? (maxEnergy - storedEnergy) / 100 : maxEnergy - storedEnergy) + load) / vLastTickHighestPrimary)) : 999999);
        extraData.putDouble("StoredEnergy", storedEnergy);
        extraData.putDouble("SecondaryVoltage", storedEnergy > 100 ? storedEnergy / 1_000 : 0);
        extraData.putDouble("LastPrimaryVoltage", vPrimary);
    }
}
