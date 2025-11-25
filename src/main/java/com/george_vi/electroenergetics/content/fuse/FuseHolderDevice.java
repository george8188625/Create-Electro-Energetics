package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterBlockEntity;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class FuseHolderDevice extends SimulatedDevice<FuseHolderDevice.DataHolder> {
    public FuseHolderDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        BridgeCollector.Builder builder = bridges.builder(pos);

        if (extraData.firstFuse != null)
            extraData.firstFuse.preTick(extraData.firstData, 1, 3, builder, level, pos);

        if (extraData.secondFuse != null)
            extraData.secondFuse.preTick(extraData.secondData, 0, 2, builder, level, pos);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {

        if (extraData.firstFuse != null) {
            extraData.firstFuse.postTick(extraData.firstData, 1, 3, results, level, pos);
        }

        if (extraData.secondFuse != null) {
            extraData.secondFuse.postTick(extraData.secondData, 0, 2, results, level, pos);
        }

        if (extraData.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof FuseHolderBlockEntity be)
                extraData.be = be;

        if (extraData.be != null) {
            if (extraData.be.isRemoved())
                extraData.be = null;
            else {
                if (level.getBlockEntity(pos) instanceof FuseHolderBlockEntity be)
                    be.updateFusesFromDevice(extraData.firstFuse == null ? null : extraData.firstData, extraData.secondFuse == null ? null : extraData.secondData);
            }
        }
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();

        ResourceLocation firstID = ResourceLocation.tryParse(tag.getString("FirstID"));
        if (firstID != null) {
            FuseHoldable type = FuseHoldable.ALL.get(firstID);
            if (type != null) {
                dataHolder.firstFuse = type;
                dataHolder.firstData = tag.getCompound("FirstData").copy();
            }
        }

        ResourceLocation secondID = ResourceLocation.tryParse(tag.getString("SecondID"));
        if (secondID != null) {
            FuseHoldable type = FuseHoldable.ALL.get(secondID);
            if (type != null) {
                dataHolder.secondFuse = type;
                dataHolder.secondData = tag.getCompound("SecondData").copy();
            }
        }

        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        if (extraData.firstFuse != null) {
            tag.putString("FirstID", extraData.firstFuse.getID().toString());
            tag.put("FirstData", extraData.firstData);
        }

        if (extraData.secondFuse != null) {
            tag.putString("FirstID", extraData.secondFuse.getID().toString());
            tag.put("FirstData", extraData.secondData);
        }
        return tag;
    }

    public static class DataHolder {
        FuseHoldable firstFuse;
        FuseHoldable secondFuse;
        CompoundTag firstData;
        CompoundTag secondData;
        FuseHolderBlockEntity be;
    }
}
