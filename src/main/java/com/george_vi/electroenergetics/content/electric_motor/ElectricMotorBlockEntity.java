package com.george_vi.electroenergetics.content.electric_motor;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.RMSHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.simibubi.create.content.kinetics.motor.KineticScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class ElectricMotorBlockEntity extends GeneratingKineticBlockEntity {

    float load = 0;
    double voltageBeforeLastChange = 0;

    protected ScrollValueBehaviour generatedSpeed;

    RMSHolder averageVoltage = new RMSHolder(16);

    @OnlyIn(Dist.CLIENT)
    protected ElectricHumSoundInstance soundInstance;


    public ElectricMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("Load", load);
        averageVoltage.write(tag, "Voltage");
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        load = tag.getFloat("Load");
        averageVoltage.read(tag, "Voltage");
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        int max = 256;
        generatedSpeed = new KineticScrollValueBehaviour(CreateLang.translateDirect("kinetics.creative_motor.rotation_speed"),
                this, new ValueBox());
        generatedSpeed.between(-max, max);
        generatedSpeed.value = 32;
        generatedSpeed.withCallback(i -> this.updateGeneratedRotation());
        behaviours.add(generatedSpeed);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.energy_consumption")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        float wattage = Math.round(averageVoltage.get() * averageVoltage.get() /
                (0.8 * Math.min(CEEConfigs.server().resistanceValues.motorResistance.get() * 3, CEEConfigs.server().resistanceValues.motorResistance.get() / Mth.clamp(load, 0.1, 3))));
        Lang.builder(CreateElectroEnergetics.ID)
                .add(CEELang.formatPower(wattage))
                .style(ChatFormatting.AQUA)
                .space()
                .add(Component.translatable("electroenergetics.gui.goggles.at_current_load")
                        .withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        return true;
    }

    @Override
    public void tick() {
        if (Math.abs(averageVoltage.get() - voltageBeforeLastChange) > 10) {
            voltageBeforeLastChange = averageVoltage.get();
            reActivateSource = true;
        }
        super.tick();
    }

    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        if (averageVoltage.get() > 60) {
            if (soundInstance == null || soundInstance.isStopped()) {
                Minecraft.getInstance()
                        .getSoundManager()
                        .play(soundInstance = new ElectricHumSoundInstance(worldPosition));
            } else if (soundInstance != null) {
                soundInstance.keepAlive();
                soundInstance.setVolume((averageVoltage.get() / (averageVoltage.get() + 1) > 1.3) || isOverStressed() ? 0.2f : 0.05f);
            }
        }
    }

    @Override
    public float calculateAddedStressCapacity() {
        float speed = Math.abs(generatedSpeed.getValue());
        float capacity = speed == 0 ? 0 : (float) ((averageVoltage.get() * averageVoltage.get()) / CEEConfigs.server().resistanceValues.motorResistance.get()) / speed;
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public float getGeneratedSpeed() {
        return convertToDirection(averageVoltage.get() < 80 ? 0 : generatedSpeed.getValue(), getBlockState().getValue(ElectricMotorBlock.FACING));
    }

    @Override
    public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
        super.updateFromNetwork(maxStress, currentStress, networkSize);
        if (maxStress == 0)
            load = 0;
        else
            load = currentStress / maxStress;
        sendData();
    }

    class ValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return Vec3.ZERO;
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            Direction facing = state.getValue(DeployerBlock.FACING);
            Vec3 vec = VecHelper.voxelSpace(8f, 8f, 16f);

            vec = VecHelper.rotateCentered(vec, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
            vec = VecHelper.rotateCentered(vec, AngleHelper.verticalAngle(getSide()), Direction.Axis.X);
            vec = vec.subtract(Vec3.atLowerCornerOf(facing.getNormal())
                    .scale(-2 / 16f));

            return vec;
        }

        @Override
        public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
            super.rotate(level, pos, state, ms);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            Direction facing = state.getValue(ElectricMotorBlock.FACING);
            return direction.getAxis() != facing.getAxis();
        }

    }
}
