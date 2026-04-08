package com.george_vi.electroenergetics.content.relay;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.cut_off_switch.SwitchingBehaviour;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class RelayDevice extends SimpleElectricalDevice {
    public SwitchingBehaviour behaviour;
    public boolean inverted;
    public boolean closed;
    public float temp;

    // 0, 1 - coil
    // 2, 3 - switched load
    public RelayDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        bridges.builder(pos)
                .resistor(0, 1, 1000);
        double r = behaviour.resistance();
        if (r < 1e+10d)
            bridges.builder(pos).resistor(2, 3, r);
    }

    @Override
    public void postTick(SimulationResults results) {

        double voltage = Math.abs(results.getVoltageAt(pos, 0, 1));
        double switchedVoltage = Math.abs(results.getVoltageAt(pos, 2, 3));
        boolean oldClosed = closed;
        if (oldClosed != (voltage > 4)) {
            if (level.isLoaded(pos))
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.1f, 1);
            closed = voltage > 4;
        }

        double lossAtArc = switchedVoltage * switchedVoltage / behaviour.resistance();

        behaviour.isClosed = closed ^ inverted;
        behaviour.postTickNoParticles(switchedVoltage / 2, pos.getCenter(), level);

        float loss = (float) ((voltage * voltage) / 1000);

        temp = updateTemp(temp, (float) Math.min(Math.max(loss, lossAtArc * 1e-4d), 10000));

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (temp > 550) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            deviceSD.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (temp > 400)
            showOverheatingParticles(level, pos);
    }

    @Override
    public void read(CompoundTag tag) {
        inverted = tag.getBoolean("Inverted");
        closed = tag.getBoolean("Closed");
        temp = tag.getFloat("Temp");
        behaviour = new SwitchingBehaviour(tag.getCompound("Behaviour"));
    }

    @Override
    public void write(CompoundTag tag) {
        if (closed)
            tag.putBoolean("Closed", true);
        if (inverted)
            tag.putBoolean("Inverted", true);
        tag.putFloat("Temp", temp);
        tag.put("Behaviour", behaviour.write());
    }


}
