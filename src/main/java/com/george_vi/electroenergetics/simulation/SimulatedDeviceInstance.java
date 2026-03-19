package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Objects;

public final class SimulatedDeviceInstance<T> {
    private final SimulatedDevice<T> simulatedDevice;
    private final BlockPos pos;
    private final T extraData;
    private final List<InWorldNode> nodes;
    private boolean valid = true;

    public SimulatedDeviceInstance(SimulatedDevice<T> simulatedDevice, BlockPos pos, T extraData,
                                   List<InWorldNode> nodes) {
        this.simulatedDevice = simulatedDevice;
        this.pos = pos;
        this.extraData = extraData;
        this.nodes = nodes;
    }

    public void invalidate() {
        valid = false;
    }

    public boolean isValid() {
        return valid;
    }

    public CompoundTag write() {
        return simulatedDevice.write(extraData);
    }

    public SimulatedDevice<T> simulatedDevice() {
        return simulatedDevice;
    }

    public BlockPos pos() {
        return pos;
    }

    public T extraData() {
        return extraData;
    }

    public List<InWorldNode> nodes() {
        return nodes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SimulatedDeviceInstance) obj;
        return Objects.equals(this.simulatedDevice, that.simulatedDevice) &&
                Objects.equals(this.pos, that.pos) &&
                Objects.equals(this.extraData, that.extraData) &&
                Objects.equals(this.nodes, that.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(simulatedDevice, pos, extraData, nodes);
    }

    @Override
    public String toString() {
        return "SimulatedDeviceInstance[" +
                "simulatedDevice=" + simulatedDevice + ", " +
                "pos=" + pos + ", " +
                "extraData=" + extraData + ", " +
                "nodes=" + nodes + ']';
    }

}
