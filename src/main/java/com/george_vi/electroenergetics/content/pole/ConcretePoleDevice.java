package com.george_vi.electroenergetics.content.pole;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.foundation.InWorldNode;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ConcretePoleDevice extends SimulatedDevice {
    public ConcretePoleDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        if (level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            if (!CEEBlocks.CONCRETE_POLE.has(state))
                return;
            extraData.putBoolean("Top", state.getValue(ConcretePoleBlock.TOP));
            extraData.putBoolean("Bottom", state.getValue(ConcretePoleBlock.BOTTOM));
        }

        if (!extraData.contains("Top"))
            return;

        if (extraData.getBoolean("Bottom")) {
            InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);

            int length = 0;
            for (int i = 1; i + pos.getY() < level.getMaxBuildHeight(); i++) {
                InfrastructureSavedData.SimulatedDeviceInstance di = sd.getDevice(pos.above(i));
                if (di == null || di.simulatedDevice() != CEESimulatedDevices.CONCRETE_POLE)
                    return;
                else if (di.extraData().getBoolean("Top")) {
                    length = i;
                    break;
                }
            }

            if (length > 0)
                bridges.bridge(new InWorldNode(0, pos), new InWorldNode(0, pos.above(length)), CEEConfigs.server().resistanceValues.wireResistance.get() * length, 0, 0);
        }

        if (extraData.getBoolean("Top") || extraData.getBoolean("Bottom"))
            bridges.builder(pos)
                    .resistor(0, 1, CEEConfigs.server().resistanceValues.wireResistance.get())
                    .resistor(1, 2, CEEConfigs.server().resistanceValues.wireResistance.get())
                    .resistor(2, 3, CEEConfigs.server().resistanceValues.wireResistance.get())
                    .resistor(3, 0, CEEConfigs.server().resistanceValues.wireResistance.get());

    }
}
