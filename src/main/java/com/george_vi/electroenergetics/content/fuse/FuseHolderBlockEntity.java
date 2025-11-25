package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
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
            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            SimulatedDeviceInstance<?> instance = sd.getDevice(getBlockPos());
            if (instance == null || !(instance.extraData() instanceof FuseHolderDevice.DataHolder dataHolder))
                return;

            if (firstFuse != null) {
                dataHolder.firstFuse = firstFuse.getFirst();
                dataHolder.firstData = firstFuse.getSecond();
            } else {
                dataHolder.firstFuse = null;
                dataHolder.firstData = null;
            }

            if (secondFuse != null) {
                dataHolder.firstFuse = secondFuse.getFirst();
                dataHolder.firstData = secondFuse.getSecond();
            } else {
                dataHolder.secondFuse = null;
                dataHolder.secondData = null;
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