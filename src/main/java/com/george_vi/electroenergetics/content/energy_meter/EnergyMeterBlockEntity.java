package com.george_vi.electroenergetics.content.energy_meter;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.UUID;

public class EnergyMeterBlockEntity extends SmartBlockEntity {
    public EnergyMeterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public float totalEnergy = 0;
    public float oldTotalEnergy = 0;
    public int ticks = 0;
    public boolean disconnected;
    public int lastPacketTick;
    public int thisPacketTick;
    public UUID owner;

    public void setTotalEnergy(float newTotalEnergy) {
        float d = (Math.abs(newTotalEnergy - totalEnergy));
        totalEnergy = newTotalEnergy;

        if ((d > 2 || ticks > 5) && !level.isClientSide) {
            sendData();
            setChanged();
            ticks = 0;
        }
        ticks++;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        oldTotalEnergy = totalEnergy;
        totalEnergy = tag.getFloat("totalEnergy");
        disconnected = tag.getBoolean("disconnected");
        if (tag.contains("owner"))
            owner = tag.getUUID("owner");

        if (clientPacket) {
            lastPacketTick = thisPacketTick;
            thisPacketTick = AnimationTickHolder.getTicks();
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        tag.putFloat("totalEnergy", totalEnergy);
        tag.putBoolean("disconnected", disconnected);
        if (owner != null)
            tag.putUUID("owner", owner);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }
}
