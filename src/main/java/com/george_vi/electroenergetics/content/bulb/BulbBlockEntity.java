package com.george_vi.electroenergetics.content.bulb;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BulbBlockEntity extends SmartBlockEntity {
    float light = 0;
    LerpedFloat smoothLight = LerpedFloat.linear();
    public BulbBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public void tick() {
        if (level.isClientSide)
            smoothLight.tickChaser();
    }

    public void setLight(float light) {
        this.light = light;
        smoothLight.chase(light, 0.5f, LerpedFloat.Chaser.LINEAR);
        if (!level.isClientSide)
            sendData();
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("Light", light);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        light = tag.getFloat("Light");
        if (clientPacket)
            smoothLight.chase(light, 0.5f, LerpedFloat.Chaser.EXP);
    }
}
