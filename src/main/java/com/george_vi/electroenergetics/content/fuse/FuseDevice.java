package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.content.cut_off_switch.SwitchingBehaviour;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.events.datagen.CEEAdvancements;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.device.ElectricalDevice;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FuseDevice extends SimpleElectricalDevice {
    public SwitchingBehaviour behaviour;
    public boolean isBroken;
    public float temp;
    public double setAmperage;

    public FuseDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        double r = behaviour.resistance();
        if (r < 1e+10d)
            bridges.builder(pos).resistor(0, 1, r);
    }

    @Override
    public void postTick(SimulationResults results) {
        double voltage = Math.abs(results.getVoltageAt(pos, 0, 1));
        double current = voltage / behaviour.resistance();

        temp = ElectricalDevice.updateTemp(temp, (float) current);

        behaviour.isClosed = !isBroken;
        behaviour.postTick(voltage / 2, pos.getCenter(), level);

        if (current < 1 || this.isBroken)
            this.temp = 0;

        if (temp > ElectricalDevice.finalTempAt((float) setAmperage)) {
            this.isBroken = true;

            if (level.isLoaded(pos)) {
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof FuseBlock fb && !fb.broken) {
                    Vec3 pPos = Vec3.atCenterOf(pos);
                    CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pPos, 40, new SendSparkPacket(pPos, SendSparkPacket.SparkSize.SMALL));
                }
            }
        }

        if (level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof FuseBlock fb && fb.broken != this.isBroken) {
                level.setBlockAndUpdate(pos, (fb.broken ? CEEBlocks.FUSE.get() : CEEBlocks.BROKEN_FUSE.get()).withPropertiesOf(state));

                Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 30, false);
                if (player != null)
                    BlownFuseTracker.onFuseBlow(player, pos);

                if (level.getBlockEntity(pos) instanceof FuseBlockEntity be) {
                    be.setAmperage = setAmperage;
                }
            }
        }
    }

    @Override
    public void read(CompoundTag tag) {
        isBroken = tag.getBoolean("Broken");
        temp = tag.getFloat("Temp");
        behaviour = new SwitchingBehaviour(tag.getCompound("Behaviour"));
        setAmperage = tag.getDouble("SetAmperage");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putBoolean("Broken", this.isBroken);
        tag.putFloat("Temp", this.temp);
        tag.put("Behaviour", behaviour.write());
        tag.putDouble("SetAmperage", setAmperage);
    }

    @Override
    public boolean shouldRemove(BlockState oldState, BlockState newState) {
        return oldState.getBlock().getClass() != newState.getBlock().getClass();
    }
}
