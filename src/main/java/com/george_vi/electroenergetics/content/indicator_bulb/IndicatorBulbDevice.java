package com.george_vi.electroenergetics.content.indicator_bulb;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class IndicatorBulbDevice extends SimpleElectricalDevice {
    public int side;
    public float oldFirstLight;
    public float oldSecondLight;
    public IndicatorBulbBlockEntity be;

    public IndicatorBulbDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (side == 0)
            bridges.builder(pos)
                    .resistor(0, 1, CEEConfigs.server().resistanceValues.indicatorBulbResistance.get());
        if (side == 1)
            bridges.builder(pos)
                    .resistor(2, 3, CEEConfigs.server().resistanceValues.indicatorBulbResistance.get());
        if (side == 2)
            bridges.builder(pos)
                    .resistor(0, 1, CEEConfigs.server().resistanceValues.indicatorBulbResistance.get())
                    .resistor(2, 3, CEEConfigs.server().resistanceValues.indicatorBulbResistance.get());

    }

    @Override
    public void postTick(SimulationResults results) {
        
        float firstLight = 0;
        float secondLight = 0;
        if (side == 0 || side == 2)
            firstLight = (float) Math.min(1, Math.abs(results.getVoltageAt(pos, 0, 1) / 70));
        if (side == 1 || side == 2)
            secondLight = (float) Math.min(1, Math.abs(results.getVoltageAt(pos, 2, 3) / 70));

        float oldSecondLight = this.oldSecondLight;
        float oldFirstLight = this.oldFirstLight;

        oldSecondLight = secondLight;
        oldFirstLight = firstLight;

        firstLight = Math.min(firstLight, oldFirstLight);
        secondLight = Math.min(secondLight, oldSecondLight);

        if (be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof IndicatorBulbBlockEntity be)
                this.be = be;

        if (be != null) {
            if (be.isRemoved())
                be = null;
            else {
                if (Math.abs(be.firstLight - firstLight) > 0.02 || Math.abs(be.secondLight - secondLight) > 0.02) {
                    be.firstLight = firstLight;
                    be.secondLight = secondLight;
                    be.sendData();
                }
            }
        }

    }

    @Override
    public void read(CompoundTag tag) {
        side = tag.getInt("Side");
        oldFirstLight = tag.getFloat("OldFirstLight");
        oldSecondLight = tag.getFloat("OldSecondLight");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putInt("Side", side);
        tag.putFloat("OldFirstLight", oldFirstLight);
        tag.putFloat("OldSecondLight", oldSecondLight);
    }

    @Override
    public boolean shouldRemove(BlockState oldState, BlockState newState) {
        return oldState.getBlock().getClass() != newState.getBlock().getClass();
    }
}
