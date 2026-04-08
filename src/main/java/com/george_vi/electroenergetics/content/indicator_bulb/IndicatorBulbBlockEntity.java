package com.george_vi.electroenergetics.content.indicator_bulb;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class IndicatorBulbBlockEntity extends SmartBlockEntity {
    DyeColor firstColor = DyeColor.WHITE;
    DyeColor secondColor = DyeColor.WHITE;
    float firstLight = 0f;
    float secondLight = 0f;

    LerpedFloat firstSmoothLight = LerpedFloat.linear();
    LerpedFloat secondSmoothLight = LerpedFloat.linear();

    public IndicatorBulbBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        firstSmoothLight.tickChaser();
        secondSmoothLight.tickChaser();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        firstColor = DyeColor.byName(tag.getString("FirstColor"), DyeColor.WHITE);
        secondColor = DyeColor.byName(tag.getString("SecondColor"), DyeColor.WHITE);
        firstLight = tag.getFloat("FirstLight");
        secondLight = tag.getFloat("SecondLight");
        if (clientPacket) {
            firstSmoothLight.chase(firstLight, 0.007, LerpedFloat.Chaser.LINEAR);
            secondSmoothLight.chase(secondLight, 0.007, LerpedFloat.Chaser.LINEAR);
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putString("FirstColor", firstColor.getSerializedName());
        tag.putString("SecondColor", secondColor.getSerializedName());
        tag.putFloat("FirstLight", firstLight);
        tag.putFloat("SecondLight", secondLight);
    }
}
