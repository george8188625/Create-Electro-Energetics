package com.george_vi.electroenergetics.content.synchroscope;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SynchroscopeBlockEntity extends SmartBlockEntity {
    float phaseOffset = 0;
    float prevPhaseOffset = 0;
    int tickLength = 0;
    int counter = 0;
    LerpedFloat smoothPhase = LerpedFloat.angular();

    public SynchroscopeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public void tick() {
        super.tick();
        counter = Mth.clamp(counter + 1, 0, 20);
        smoothPhase.tickChaser();
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (clientPacket) {
            prevPhaseOffset = phaseOffset;
            tickLength = Math.max(1, counter);
            counter = 0;
        }
        smoothPhase.chase(phaseOffset, 1, LerpedFloat.Chaser.LINEAR);
        phaseOffset = tag.getFloat("PhaseOffset");
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("PhaseOffset", phaseOffset);
    }
}
