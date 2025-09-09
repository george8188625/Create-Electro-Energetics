package com.george_vi.electroenergetics.content.railway_electrification.catenary;

import com.george_vi.electroenergetics.content.pole.ConcretePoleBlock;
import com.simibubi.create.content.decoration.girder.GirderBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class CatenaryHolderBlockEntity extends SmartBlockEntity {

    static final Iterable<BlockPos.MutableBlockPos> offsets = BlockPos.spiralAround(BlockPos.ZERO, 4, Direction.EAST, Direction.SOUTH);
    BlockPos attachedTo;

    int poleWidth = 0;

    public CatenaryHolderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public void tick() {
        if (attachedTo == null || attachedTo.getCenter().length() > 7 || !countsAsPole(attachedTo.offset(worldPosition))) {
            BlockPos polePos = null;
            float shortestDistance = 999;
            for (BlockPos.MutableBlockPos offset : offsets) {
                BlockPos pos = offset.immutable();
                float distance = (float) pos.getCenter().length();
                if (distance < shortestDistance && countsAsPole(pos.offset(worldPosition))) {
                    polePos = pos;
                    shortestDistance = distance;
                }
            }
            if (polePos != null) {
                if (!polePos.equals(attachedTo))
                    invalidateRenderBoundingBox();
                attachedTo = polePos;
            } else
                attachedTo = null;
        }
    }

    @Override
    protected AABB createRenderBoundingBox() {
        if (attachedTo == null)
            return super.createRenderBoundingBox();
        return AABB.encapsulatingFullBlocks(worldPosition, worldPosition.offset(attachedTo));
    }

    boolean countsAsPole(BlockPos pos) {
        poleWidth = getPoleWidth(pos);
        return poleWidth > 0;
    }

    int getPoleWidth(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof FenceBlock)
            return 4;
        if (state.getBlock() instanceof GirderBlock || state.getBlock() instanceof WallBlock)
            return 8;
        if (state.getBlock() instanceof ConcretePoleBlock)
            return 10;
        return 0;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        attachedTo = NbtUtils.readBlockPos(tag, "AttachedTo").orElse(null);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (attachedTo != null)
            tag.put("AttachedTo", NbtUtils.writeBlockPos(attachedTo));
    }

    public BlockPos getAttachedTo() {
        return attachedTo;
    }
}
