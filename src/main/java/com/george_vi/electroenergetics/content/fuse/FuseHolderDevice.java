package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.content.fuse.fuse_held.FuseHoldable;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class FuseHolderDevice extends SimpleElectricalDevice {

    public FuseHolderDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    FuseHoldable firstFuse;
    FuseHoldable secondFuse;
    CompoundTag firstData;
    CompoundTag secondData;
    FuseHolderBlockEntity be;

    @Override
    public void preTick(BridgeCollector bridges) {
        BridgeCollector.Builder builder = bridges.builder(pos);

        if (this.firstFuse != null)
            this.firstFuse.preTick(this.firstData, 1, 3, builder, level, pos);

        if (this.secondFuse != null)
            this.secondFuse.preTick(this.secondData, 0, 2, builder, level, pos);
    }

    @Override
    public void postTick(SimulationResults results) {
        if (this.firstFuse != null) {
            this.firstFuse.postTick(this.firstData, 1, 3, results, level, pos);
        }

        if (this.secondFuse != null) {
            this.secondFuse.postTick(this.secondData, 0, 2, results, level, pos);
        }

        if (this.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof FuseHolderBlockEntity be)
                this.be = be;

        if (this.be != null) {
            if (this.be.isRemoved())
                this.be = null;
            else {
                if (level.getBlockEntity(pos) instanceof FuseHolderBlockEntity be)
                    be.updateFusesFromDevice(this.firstFuse == null ? null : this.firstData, this.secondFuse == null ? null : this.secondData);
            }
        }
    }

    @Override
    public void read(CompoundTag tag) {

        ResourceLocation firstID = ResourceLocation.tryParse(tag.getString("FirstID"));
        if (firstID != null) {
            FuseHoldable type = FuseHoldable.ALL.get(firstID);
            if (type != null) {
                this.firstFuse = type;
                this.firstData = tag.getCompound("FirstData").copy();
            }
        }

        ResourceLocation secondID = ResourceLocation.tryParse(tag.getString("SecondID"));
        if (secondID != null) {
            FuseHoldable type = FuseHoldable.ALL.get(secondID);
            if (type != null) {
                this.secondFuse = type;
                this.secondData = tag.getCompound("SecondData").copy();
            }
        }
    }

    @Override
    public void write(CompoundTag tag) {
        if (this.firstFuse != null) {
            tag.putString("FirstID", this.firstFuse.getID().toString());
            tag.put("FirstData", this.firstData);
        }

        if (this.secondFuse != null) {
            tag.putString("SecondID", this.secondFuse.getID().toString());
            tag.put("SecondData", this.secondData);
        }
    }
}
