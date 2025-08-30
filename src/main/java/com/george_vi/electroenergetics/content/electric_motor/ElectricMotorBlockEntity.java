package com.george_vi.electroenergetics.content.electric_motor;

import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.george_vi.electroenergetics.content.wire.WireRenderer;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.Node;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.kinetics.motor.KineticScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangNumberFormat;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class ElectricMotorBlockEntity extends GeneratingKineticBlockEntity {

    float voltage = 0;
    List<Float> voltages = new ArrayList<>();
    float avgVoltage = 0;
    float load = 0;
    float voltageBeforeLastChange = 0;

    protected ScrollValueBehaviour generatedSpeed;

    @OnlyIn(Dist.CLIENT)
    protected ElectricHumSoundInstance soundInstance;

    public ElectricMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(8);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("Load", load);
        tag.putFloat("Voltage", voltage);
        tag.put("Voltages", NBTHelper.writeCompoundList(voltages, (v) -> {
            CompoundTag t = new CompoundTag();
            t.putFloat("V", v);
            return t;
        }));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        load = tag.getFloat("Load");
        voltage = tag.getFloat("Voltage");
        voltages = NBTHelper.readCompoundList(tag.getList("Voltages", Tag.TAG_COMPOUND), t -> t.getFloat("V"));
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
        if (Math.abs(avgVoltage) < 0.1)
            return false;
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate("gui.goggles.energy_consumption")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        float wattage = Math.round(avgVoltage * avgVoltage /
                Math.min(CEEConfigs.server().resistanceValues.motorResistance.get() * 3, CEEConfigs.server().resistanceValues.motorResistance.get() / Mth.clamp(load, 0.1, 3)));
        Lang.builder(CreateElecrtoEnergetics.ID)
                .add(CEELang.formatPower(wattage))
                .style(ChatFormatting.AQUA)
                .space()
                .add(Component.translatable("electroenergetics.gui.goggles.at_current_load")
                        .withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        return true;
    }

    int tick = 0;
    @Override
    public void tick() {
        if (voltages.isEmpty())
            avgVoltage = 0;
        else
            avgVoltage = voltages.stream().reduce(Float::sum).orElse(0f) / voltages.size();

        super.tick();
        tick++;
    }

    @Override
    public void lazyTick() {
        if (Math.abs(avgVoltage - voltageBeforeLastChange) > 10) {
            voltageBeforeLastChange = avgVoltage;
            reActivateSource = true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        Float v1 = WireRenderer.getAllVoltages().get(new Node(0, getBlockPos()));
        Float v2 = WireRenderer.getAllVoltages().get(new Node(1, getBlockPos()));
        if (v1 != null && v2 != null)
            setVoltage(v1 - v2);
        if (Math.abs(avgVoltage) > 60) {
            if (soundInstance == null || soundInstance.isStopped()) {
                Minecraft.getInstance()
                        .getSoundManager()
                        .play(soundInstance = new ElectricHumSoundInstance(worldPosition));
            } else if (soundInstance != null) {
                soundInstance.keepAlive();
                soundInstance.setVolume((Math.abs(voltage) / (Math.abs(avgVoltage) + 1) > 1.3) || isOverStressed() ? 0.2f : 0.05f);
            }
        }
    }

    @Override
    public float calculateAddedStressCapacity() {
        float speed = Math.abs(generatedSpeed.getValue());
        float capacity = speed == 0 ? 0 : (float) ((avgVoltage * avgVoltage) / CEEConfigs.server().resistanceValues.motorResistance.get()) / speed;
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public float getGeneratedSpeed() {
        return convertToDirection(Math.abs(avgVoltage) < 80 ? 0 : generatedSpeed.getValue(), getBlockState().getValue(ElectricMotorBlock.FACING));
    }

    public void setVoltage(float voltage) {
        if (voltages.size() >= 10)
            voltages.remove(0);
        voltages.add(voltage);
        this.voltage = voltage;
    }

    @Override
    public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
        super.updateFromNetwork(maxStress, currentStress, networkSize);
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
