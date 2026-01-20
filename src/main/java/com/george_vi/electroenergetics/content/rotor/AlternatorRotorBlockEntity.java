package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.CEEBlocks;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AlternatorRotorBlockEntity extends KineticBlockEntity {
    int magnets = 0;

    public AlternatorRotorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        setLazyTickRate(10);
    }

    protected Direction.Axis getAxis() {
        Direction.Axis axis = Direction.Axis.X;
        BlockState blockState = getBlockState();
        if (blockState.getBlock()instanceof IRotate irotate)
            axis = irotate.getRotationAxis(blockState);
        return axis;
    }

    @Override
    public void lazyTick() {
        int magnets = 0;

        for (BlockPos blockPos : WaterWheelBlockEntity.LARGE_OFFSETS.get(getAxis()))
            if (CEEBlocks.MAGNET_BLOCK.has(level.getBlockState(blockPos.offset(worldPosition))))
                magnets++;

        if (this.magnets == magnets)
            return;

        this.magnets = magnets;
        if (hasNetwork()) {
            KineticNetwork network = getOrCreateNetwork();
            network.updateStressFor(this, calculateStressApplied());
            // For some reason when updating stress, create does something and sometimes that the client thinks the block is overstressed, when it's not on the server. This updates it.
            sendData();
        }
    }

    @Override
    public float calculateStressApplied() {
        float impact = (magnets + 0.125f) * 48;
        this.lastStressApplied = impact;
        return impact;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("Magnets", magnets);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        magnets = tag.getInt("Magnets");
    }
}
