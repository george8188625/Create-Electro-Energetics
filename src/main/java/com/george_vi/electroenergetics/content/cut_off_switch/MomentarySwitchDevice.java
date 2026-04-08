package com.george_vi.electroenergetics.content.cut_off_switch;

import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MomentarySwitchDevice extends SimpleElectricalDevice {
    public SwitchingBehaviour behaviour;
    public int closedTicks;

    public MomentarySwitchDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
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
        Vec3 pPos = null;
        if (level.isLoaded(pos)) {
            pPos = Vec3.atCenterOf(pos);
            BlockState blockState = level.getBlockState(pos);
            pPos = pPos.subtract(Vec3.atLowerCornerOf(blockState.getValue(CutOffSwitchBlock.FACING).getNormal()).multiply(0.125, 0.125, 0.125));
            if (this.closedTicks == 0) {
                if (blockState.getBlock() instanceof MomentarySwitchBlock block)
                    block.openSwitch(blockState, (ServerLevel) level, pos);
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.1f, 1);
            }
        }

        this.closedTicks = Math.max(-1, this.closedTicks - 1);
        this.behaviour.isClosed = this.closedTicks > 0;
        this.behaviour.postTick(results.getVoltageAt(pos, 0, 1), pPos, level);
    }

    @Override
    public void read(CompoundTag tag) {
        closedTicks = tag.getInt("ClosedTicks");
        behaviour = new SwitchingBehaviour(tag.getCompound("Behaviour"));
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putInt("ClosedTicks", closedTicks);
        tag.put("Behaviour", behaviour.write());
    }
}
