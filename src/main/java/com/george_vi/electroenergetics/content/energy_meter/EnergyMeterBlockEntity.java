package com.george_vi.electroenergetics.content.energy_meter;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.animation.LerpedFloat;
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


    public double activePower = 0;
    public double totalEnergy = -1;
    public LerpedFloat smoothTotalEnergy = LerpedFloat.linear();
    public int ticks = 0;
    public boolean disconnected;
    public UUID owner;

    public void setTotalEnergy(double newTotalEnergy) {
        double d = (Math.abs(newTotalEnergy - totalEnergy));
        totalEnergy = newTotalEnergy;

        if ((d > 2 || ticks > 5) && !level.isClientSide) {
            sendData();
            setChanged();
            ticks = 0;
        }
        ticks++;
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide)
            smoothTotalEnergy.tickChaser();
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        boolean first = totalEnergy == -1;
        totalEnergy = tag.getDouble("TotalEnergy");
        activePower = tag.getDouble("ActivePower");
        disconnected = tag.getBoolean("Disconnected");
        if (tag.contains("Owner"))
            owner = tag.getUUID("Owner");

        if (clientPacket)
            smoothTotalEnergy.chase(totalEnergy, first ? 1 : 0.5, LerpedFloat.Chaser.EXP);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        tag.putDouble("TotalEnergy", totalEnergy);
        tag.putDouble("ActivePower", activePower);
        tag.putBoolean("Disconnected", disconnected);
        if (owner != null)
            tag.putUUID("Owner", owner);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }
}
