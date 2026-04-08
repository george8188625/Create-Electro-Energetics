package com.george_vi.electroenergetics.content.redstone_relay;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.content.cut_off_switch.SwitchingBehaviour;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneRelayDevice extends SimpleElectricalDevice {
    public SwitchingBehaviour behaviour;
    public boolean inverted;
    public boolean powered;

    public RedstoneRelayDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            if (CEEBlocks.REDSTONE_RELAY.has(state)) {
                this.powered = state.getValue(RedstoneRelayBlock.POWERED);
                this.inverted = state.getValue(RedstoneRelayBlock.INVERTED);
            }
        }

        double r = behaviour.resistance();
        if (r < 1e+10d)
            bridges.builder(pos).resistor(0, 1, r);
    }

    @Override
    public void postTick(SimulationResults results) {
        this.behaviour.isClosed = this.powered ^ this.inverted;
        this.behaviour.postTickNoParticles(results.getVoltageAt(pos, 0, 1) / 2, pos.getCenter(), level);
    }

    @Override
    public void read(CompoundTag tag) {
        inverted = tag.getBoolean("Inverted");
        powered = tag.getBoolean("Powered");
        behaviour = new SwitchingBehaviour(tag.getCompound("Behaviour"));
    }

    @Override
    public void write(CompoundTag tag) {
        if (this.powered)
            tag.putBoolean("Powered", true);
        if (this.inverted)
            tag.putBoolean("Inverted", true);
        tag.put("Behaviour", behaviour.write());
    }
}
