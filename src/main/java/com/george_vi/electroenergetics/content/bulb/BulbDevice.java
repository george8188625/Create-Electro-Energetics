package com.george_vi.electroenergetics.content.bulb;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BulbDevice extends SimpleElectricalDevice {

    public float temp;
    public boolean destroyed;
    public BulbBlockEntity be;

    public BulbDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (!destroyed)
            bridges.builder(pos).resistor(0, 1, CEEConfigs.server().resistanceValues.bulbResistance.get());
    }


    @Override
    public void postTick(SimulationResults results) {

        double vd = Math.abs(results.getVoltageAt(pos, 0, 1));
        if (level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            if (destroyed) {
                if (CEEBlocks.BULB.has(state)) {
                    level.setBlockAndUpdate(pos, CEEBlocks.BROKEN_BULB.get().withPropertiesOf(state));
                    Vec3 pPos = Vec3.atCenterOf(pos);
                    CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pPos, 40, new SendSparkPacket(pPos, SendSparkPacket.SparkSize.SMALL));
                }
                return;
            }

            int light = getLightLevel(vd);

            if (state.getBlock() instanceof BulbBlock) {
                int blockLight = state.getValue(BulbBlock.LIGHT);
                if (blockLight != light)
                    level.setBlockAndUpdate(pos, state.setValue(BulbBlock.LIGHT, light));

                if (be == null)
                    if (level.getBlockEntity(pos) instanceof BulbBlockEntity nbe)
                        this.be = nbe;

                if (be != null) {
                    if (be.isRemoved())
                        be = null;
                    else {
                        float newLight = (float) Math.min(1, vd / 500);
                        newLight = 1 - (1 - newLight) * (1 - newLight);

                        if (Math.abs(be.light - newLight) > 0.025) {
                            be.light = newLight;
                            be.sendData();
                        }
                    }
                }
            }
        }
        float loss = (float) (vd * vd / CEEConfigs.server().resistanceValues.bulbResistance.get());
        temp = updateTemp(temp, loss);

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (temp > 2900) {
            destroyed = true;
            temp = 0;
        }
    }

    private int getLightLevel(double vd) {
        return Mth.floor(Math.min(Math.round(vd / 19), 15));
    }

    @Override
    public void read(CompoundTag tag) {
        temp = tag.getFloat("Temp");
        destroyed = tag.getBoolean("Destroyed");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putFloat("Temp", temp);
        tag.putBoolean("Destroyed", destroyed);
    }

    @Override
    public boolean shouldRemove(BlockState oldState, BlockState newState) {
        return oldState.getBlock().getClass() != newState.getBlock().getClass();
    }
}
