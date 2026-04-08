package com.george_vi.electroenergetics.content.cut_off_switch;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CutOffSwitchDevice extends SimpleElectricalDevice {
    final int lines;
    public SwitchingBehaviour[] behaviours;
    public boolean isClosed;

    public CutOffSwitchDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type, int lines) {
        super(level, pos, deviceSD, type);
        this.lines = lines;
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        for (int i = 0; i < lines; i++) {

            SwitchingBehaviour behaviour = behaviours[i];
            double r = behaviour.resistance();
            if (r < 1e+10d)
                bridges.builder(pos).resistor(i, (lines) + i, r);
        }
    }

    @Override
    public void postTick(SimulationResults results) {
        super.postTick(results);
        Vec3 pPos = null;
        if (level.isLoaded(pos)) {
            pPos = Vec3.atCenterOf(pos);
            BlockState blockState = level.getBlockState(pos);
            if (CEEBlocks.FUSE.has(blockState))
                pPos = pPos.subtract(Vec3.atLowerCornerOf(blockState.getValue(CutOffSwitchBlock.FACING).getNormal()).multiply(0.125, 0.125, 0.125));
        }

        for (int i = 0; i < lines; i++) {
            SwitchingBehaviour behaviour = behaviours[i];
            behaviour.isClosed = isClosed;
            behaviour.postTick(results.getVoltageAt(pos, i, lines + i), pPos, level);
        }
    }

    @Override
    public void read(CompoundTag tag) {
        behaviours = new SwitchingBehaviour[lines];
        isClosed = tag.getBoolean("Closed");
        for (int i = 0; i < lines; i++)
            behaviours[i] = new SwitchingBehaviour(tag.getCompound("Switch_"+i));
    }

    @Override
    public void write(CompoundTag tag) {
        if (isClosed)
            tag.putBoolean("Closed", true);
        for (int i = 0; i < lines; i++)
            tag.put("Switch_"+i, behaviours[i].write());
    }
}
