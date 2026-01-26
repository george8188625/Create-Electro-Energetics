package com.george_vi.electroenergetics.content.railway_electrification.pantograph;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class PantographBlockEntity extends SmartBlockEntity {
    public float prevExtensionState = 0;
    public float currentExtensionState = 0;
    public float targetExtensionState = 0.85f;
    public DyeColor color = DyeColor.WHITE;


    public PantographBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public void tick() {
        super.tick();
        prevExtensionState = currentExtensionState;
        currentExtensionState = Mth.lerp(0.1f, currentExtensionState, targetExtensionState);
        if (Math.abs(currentExtensionState - targetExtensionState) < 0.01)
            currentExtensionState = targetExtensionState;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        color = DyeColor.byName(tag.getString("Color"), DyeColor.WHITE);
        targetExtensionState = tag.getFloat("TargetExtensionState");
        currentExtensionState = tag.getFloat("ExtensionState");
        prevExtensionState = tag.getFloat("PrevExtensionState");
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putString("Color", color.getSerializedName());
        tag.putFloat("TargetExtensionState", targetExtensionState);
        tag.putFloat("ExtensionState", currentExtensionState);
        tag.putFloat("PrevExtensionState", prevExtensionState);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(2);
    }
}
