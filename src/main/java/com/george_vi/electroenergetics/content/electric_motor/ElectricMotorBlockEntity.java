package com.george_vi.electroenergetics.content.electric_motor;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.RMSHolder;
import com.george_vi.electroenergetics.foundation.scroll_value.KineticUnlockableScrollValueBehaviour;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.animation.LerpedFloat;
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
    float speedBeforeLastChange = 0;

    float motorSpeed;

    protected KineticUnlockableScrollValueBehaviour generatedSpeed;

    RMSHolder averageVoltage;

    @OnlyIn(Dist.CLIENT)
    protected ElectricHumSoundInstance soundInstance;

    @OnlyIn(Dist.CLIENT)
    protected ElectricHumSoundInstance windSoundInstance;

    LerpedFloat soundSpeed = LerpedFloat.linear();


    public ElectricMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(12);
        averageVoltage = new RMSHolder(8 * (1 << CEEConfigs.server().simulationConfig.microTickBits.get()));
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (generatedSpeed.isUnlocked()) {
            motorSpeed = Mth.lerp(1f, motorSpeed, (float) Mth.clamp(averageVoltage.getSigned() / 4, -256, 256));
            if (Math.abs(motorSpeed - speedBeforeLastChange) > 2) {
                voltageBeforeLastChange = averageVoltage.getSigned();
                speedBeforeLastChange = motorSpeed;
                reActivateSource = true;
            }
        } else {
            motorSpeed = averageVoltage.get() > 80 ? generatedSpeed.getValue() : 0;

            if (Math.abs(averageVoltage.getSigned() - voltageBeforeLastChange) > 2) {
                voltageBeforeLastChange = averageVoltage.getSigned();
                speedBeforeLastChange = motorSpeed;
                reActivateSource = true;
            }
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("Load", load);
        tag.putFloat("MotorSpeed", motorSpeed);
        if (!clientPacket) {
            tag.putDouble("VoltageBeforeLastChange", voltageBeforeLastChange);
            tag.putFloat("SpeedBeforeLastChange", speedBeforeLastChange);
        }
        averageVoltage.write(tag, "Voltage");
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        load = tag.getFloat("Load");
        motorSpeed = tag.getFloat("MotorSpeed");
        if (!clientPacket) {
            voltageBeforeLastChange = tag.getDouble("VoltageBeforeLastChange");
            speedBeforeLastChange = tag.getFloat("SpeedBeforeLastChange");
        }
        averageVoltage.read(tag, "Voltage");
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        int max = 256;
        generatedSpeed = new KineticUnlockableScrollValueBehaviour(CreateLang.translateDirect("kinetics.creative_motor.rotation_speed"),
                this, new ValueBox());
        generatedSpeed.between(-max, max);
        generatedSpeed.value = 32;
        generatedSpeed.withCallback(i -> updateRotation());
        behaviours.add(generatedSpeed);
    }

    public void updateRotation() {
        if (generatedSpeed.isUnlocked()) {
            motorSpeed = (float) Mth.clamp(averageVoltage.getSigned() / 2, -256, 256);
            if (Math.abs(motorSpeed - speedBeforeLastChange) > 2) {
                voltageBeforeLastChange = averageVoltage.getSigned();
                speedBeforeLastChange = motorSpeed;
                reActivateSource = true;
            }
        } else {
            motorSpeed = generatedSpeed.getValue();

            voltageBeforeLastChange = averageVoltage.getSigned();
            speedBeforeLastChange = motorSpeed;
            reActivateSource = true;
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
//        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (speed == 0)
            return false;

        // Maximum capacity
        float stressBase = calculateAddedStressCapacity();

        CreateLang.translate("gui.goggles.generator_stats")
                .forGoggles(tooltip);
        CEELang.builder()
                .translate("gui.goggles.maximum_capacity")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        float speed = getTheoreticalSpeed();
        if (speed != getGeneratedSpeed() && speed != 0)
            stressBase *= getGeneratedSpeed() / speed;

        float stressTotal = Math.abs(stressBase * speed);

        CreateLang.number(stressTotal)
                .translate("generic.unit.stress")
                .style(ChatFormatting.AQUA)
                .space()
                .add(CEELang.translate("gui.goggles.at_current_voltage")
                        .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        // Current generation

        CEELang.builder()
                .translate("gui.goggles.current_generation")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        CreateLang.number(load * stressTotal)
                .translate("generic.unit.stress")
                .style(ChatFormatting.AQUA)
                .space()
                .add(CreateLang.translate("gui.goggles.at_current_speed")
                        .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        // Energy consumption
        double load = Mth.clamp(this.load, 0.05, 1);
        float wattage = Math.round(averageVoltage.get() * averageVoltage.get() /
                (0.8 * Math.min(CEEConfigs.server().resistanceValues.motorResistance.get() * 6,
                        CEEConfigs.server().resistanceValues.motorResistance.get() / load)));

        CEELang.builder()
                .translate("gui.goggles.energy_consumption")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CEELang.builder()
                .add(CEELang.formatPower(wattage))
                .style(ChatFormatting.AQUA)
                .space()
                .add(CEELang.translate("gui.goggles.at_current_load")
                        .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        return true;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        float speed = isOverStressed() ? 0 : this.speed;
        soundSpeed.chase(speed, Math.abs(soundSpeed.getValue()) > Math.abs(speed) || generatedSpeed.isUnlocked() ? 5 : 1, LerpedFloat.Chaser.LINEAR);
        soundSpeed.tickChaser();
        float speedNormalized = Math.abs(soundSpeed.getValue()) / 256f;
        if (averageVoltage.get() > 67) {
            if (soundInstance == null || soundInstance.isStopped()) {
                Minecraft.getInstance()
                        .getSoundManager()
                        .play(soundInstance = new ElectricHumSoundInstance(CEESoundEvents.DC_TRAIN.get(), worldPosition));
            } else if (soundInstance != null && speedNormalized != 0) {
                soundInstance.keepAlive();
                soundInstance.setVolume(Math.min(0.5f, speedNormalized * 0.75f));
                soundInstance.setPitch(Mth.lerp(speedNormalized, 0.1f, 2));
            }
        }
        if (windSoundInstance == null || windSoundInstance.isStopped()) {
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
    public float calculateAddedStressCapacity() {
        float speed = Math.abs(getMotorSpeed());
        float capacity = speed == 0 ? 0 : (float) ((averageVoltage.get() * averageVoltage.get()) / CEEConfigs.server().resistanceValues.motorResistance.get()) / speed;
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public float getGeneratedSpeed() {
        return convertToDirection(averageVoltage.get() < 80 ? 0 : getMotorSpeed(), getBlockState().getValue(ElectricMotorBlock.FACING));
    }

    private float getMotorSpeed() {
        return motorSpeed;
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

    @Override
    public int getFlickerScore() {
        // Makes create not destroy the block when it's speed is repeatedly changed.
        return 0;
    }

    static class ValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return Vec3.ZERO;
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            Direction facing = state.getValue(DeployerBlock.FACING);
            Vec3 vec = VecHelper.voxelSpace(8f, 8f, 15f);

            vec = VecHelper.rotateCentered(vec, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
            vec = VecHelper.rotateCentered(vec, AngleHelper.verticalAngle(getSide()), Direction.Axis.X);
            vec = vec.subtract(Vec3.atLowerCornerOf(facing.getNormal())
                    .scale(2 / 16f));

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
