package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.config.CRotor;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class AlternatorRotorBlockEntity extends KineticBlockEntity {
    int magnets = 0;

    @OnlyIn(Dist.CLIENT)
    protected ElectricHumSoundInstance soundInstance;

    @OnlyIn(Dist.CLIENT)
    protected ElectricHumSoundInstance windSoundInstance;

    LerpedFloat soundSpeed = LerpedFloat.linear();

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

        Direction.Axis rotorAxis = getBlockState().getValue(AlternatorRotorBlock.AXIS);
        for (Direction direction : Iterate.directions) {
            if (direction.getAxis() == rotorAxis)
                continue;
            BlockPos statorPos = worldPosition.relative(direction);

            BlockState statorState = level.getBlockState(statorPos);
            if (CEEBlocks.STATOR.has(statorState) &&
                    StatorBlock.canPowerRotor(statorPos, statorState, getBlockPos(), getBlockState()))
                magnets += 3;
        }

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


    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        float speed = isOverStressed() ? 0 : this.speed;
        soundSpeed.chase(speed, Math.abs(soundSpeed.getValue()) > Math.abs(speed) ? 5 : 2, LerpedFloat.Chaser.LINEAR);
        soundSpeed.tickChaser();
        float speedNormalized = Math.abs(soundSpeed.getValue()) / 256f;
        if (Math.abs(speedNormalized) < 0.1f)
            return;

        if (soundInstance == null || soundInstance.isStopped() ||
                !Minecraft.getInstance().getSoundManager().isActive(soundInstance)) {
            Minecraft.getInstance()
                    .getSoundManager()
                    .play(soundInstance = new ElectricHumSoundInstance(CEESoundEvents.DC_TRAIN.get(), worldPosition));
        } else if (soundInstance != null && speedNormalized != 0) {
            soundInstance.keepAlive();
            soundInstance.setVolume(Math.min(0.5f, speedNormalized * 0.75f));
            soundInstance.setPitch(Mth.lerp(speedNormalized, 0.1f, 2));
        }

        if (windSoundInstance == null || windSoundInstance.isStopped() ||
                !Minecraft.getInstance().getSoundManager().isActive(windSoundInstance)) {
            Minecraft.getInstance()
                    .getSoundManager()
                    .play(windSoundInstance = new ElectricHumSoundInstance(CEESoundEvents.TRAIN_WIND_STATIC.get(), worldPosition));
        } else if (windSoundInstance != null && speedNormalized != 0) {
            windSoundInstance.keepAlive();
            windSoundInstance.setVolume(speedNormalized * 0.5f);
            windSoundInstance.setPitch(Mth.lerp(speedNormalized, 0.1f, 1.5f));
        }
    }

    @Override
    public float calculateStressApplied() {
        CRotor rotorConfig = CEEConfigs.server().rotorValues;
        float impact = (magnets + 0.125f)
                * rotorConfig.rotorPowerMultiplier.getF()
                * rotorConfig.rotorStressMultiplier.getF();
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
