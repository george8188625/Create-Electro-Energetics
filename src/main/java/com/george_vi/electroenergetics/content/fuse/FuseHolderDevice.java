package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class FuseHolderDevice extends SimulatedDevice {
    public FuseHolderDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        Pair<FuseHoldable, CompoundTag> firstFuse;
        Pair<FuseHoldable, CompoundTag> secondFuse;

        ResourceLocation firstID = ResourceLocation.tryParse(extraData.getString("FirstID"));
        if (firstID != null) {
            FuseHoldable type = FuseHoldable.ALL.get(firstID);
            if (type != null) {
                firstFuse = Pair.of(type, extraData.getCompound("FirstData"));
            } else
                firstFuse = null;
        } else
            firstFuse = null;

        ResourceLocation secondID = ResourceLocation.tryParse(extraData.getString("SecondID"));
        if (secondID != null) {
            FuseHoldable type = FuseHoldable.ALL.get(secondID);
            if (type != null) {
                secondFuse = Pair.of(type, extraData.getCompound("SecondData"));
            } else
                secondFuse = null;
        } else
            secondFuse = null;

        BridgeCollector.Builder builder = bridges.builder(pos);

        if (firstFuse != null) {
            firstFuse.getFirst().preTick(firstFuse.getSecond(), 1, 3, builder, level, pos);
        }

        if (secondFuse != null) {
            secondFuse.getFirst().preTick(secondFuse.getSecond(), 0, 2, builder, level, pos);
        }
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        Pair<FuseHoldable, CompoundTag> firstFuse;
        Pair<FuseHoldable, CompoundTag> secondFuse;

        ResourceLocation firstID = ResourceLocation.tryParse(extraData.getString("FirstID"));
        if (firstID != null) {
            FuseHoldable type = FuseHoldable.ALL.get(firstID);
            if (type != null) {
                firstFuse = Pair.of(type, extraData.getCompound("FirstData"));
            } else
                firstFuse = null;
        } else
            firstFuse = null;

        ResourceLocation secondID = ResourceLocation.tryParse(extraData.getString("SecondID"));
        if (secondID != null) {
            FuseHoldable type = FuseHoldable.ALL.get(secondID);
            if (type != null) {
                secondFuse = Pair.of(type, extraData.getCompound("SecondData"));
            } else
                secondFuse = null;
        } else
            secondFuse = null;


        if (firstFuse != null) {
            firstFuse.getFirst().postTick(firstFuse.getSecond(), 1, 3, results, level, pos);
        }

        if (secondFuse != null) {
            secondFuse.getFirst().postTick(secondFuse.getSecond(), 0, 2, results, level, pos);
        }

        if (level.isLoaded(pos)) {
            if (level.getBlockEntity(pos) instanceof FuseHolderBlockEntity be) {
                be.updateFusesFromDevice(firstFuse == null ? null : firstFuse.getSecond(), secondFuse == null ? null : secondFuse.getSecond());
            }
        }
    }
}
