package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.content.fuse.fuse_held.FuseHoldable;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class FuseHolderBlockEntity extends SmartBlockEntity {
    Pair<FuseHoldable, CompoundTag> firstFuse;
    Pair<FuseHoldable, CompoundTag> secondFuse;

    public FuseHolderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (firstFuse != null) {
            tag.putString("FirstID", firstFuse.getFirst().getID().toString());
            tag.put("FirstData", firstFuse.getSecond());
        }
        if (secondFuse != null) {
            tag.putString("SecondID", secondFuse.getFirst().getID().toString());
            tag.put("SecondData", secondFuse.getSecond());
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        ResourceLocation firstID = ResourceLocation.tryParse(tag.getString("FirstID"));
        if (firstID != null) {
            FuseHoldable type = FuseHoldable.ALL.get(firstID);
            if (type != null) {
                firstFuse = Pair.of(type, tag.getCompound("FirstData"));
            } else
                firstFuse = null;
        } else
            firstFuse = null;

        ResourceLocation secondID = ResourceLocation.tryParse(tag.getString("SecondID"));
        if (secondID != null) {
            FuseHoldable type = FuseHoldable.ALL.get(secondID);
            if (type != null) {
                secondFuse = Pair.of(type, tag.getCompound("SecondData"));
            } else
                secondFuse = null;
        } else
            secondFuse = null;
    }

    public void updateFuses() {
        sendData();
        if (level instanceof ServerLevel sl) {
            FuseHolderDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, FuseHolderDevice.class);
            if (device == null)
                return;

            if (firstFuse != null) {
                device.firstFuse = firstFuse.getFirst();
                device.firstData = firstFuse.getSecond();
            } else {
                device.firstFuse = null;
                device.firstData = null;
            }

            if (secondFuse != null) {
                device.secondFuse = secondFuse.getFirst();
                device.secondData = secondFuse.getSecond();
            } else {
                device.secondFuse = null;
                device.secondData = null;
            }
        }
    }

    public void updateFusesFromDevice(CompoundTag firstData, CompoundTag secondData) {
        boolean update = false;
        if (firstFuse != null && firstData != null) {
            if (firstData.getBoolean("UpdateThisTick")) {
                firstData.remove("UpdateThisTick");
                update = true;
            }
            firstFuse = Pair.of(firstFuse.getFirst(), firstData);
        }
        if (secondFuse != null && secondData != null) {
            if (secondData.getBoolean("UpdateThisTick")) {
                secondData.remove("UpdateThisTick");
                update = true;
            }
            secondFuse = Pair.of(secondFuse.getFirst(), secondData);
        }

        if (update)
            sendData();
    }
}