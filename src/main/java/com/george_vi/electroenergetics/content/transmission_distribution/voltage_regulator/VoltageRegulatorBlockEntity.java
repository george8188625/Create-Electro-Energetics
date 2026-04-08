package com.george_vi.electroenergetics.content.transmission_distribution.voltage_regulator;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.VoltageScrollValueBehaviour;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangNumberFormat;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class VoltageRegulatorBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public VoltageRegulatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    protected VoltageScrollValueBehaviour voltage;
    protected double power;
    protected double lastSentPower = -1;
    protected double inputVoltage;
    protected double outputVoltage;

    @OnlyIn(Dist.CLIENT)
    protected ElectricHumSoundInstance soundInstance;

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        if (!getBlockState().getValue(VoltageRegulatorBlock.BOTTOM)) {
            for (int i = 1;; i++) {
                BlockPos pos = worldPosition.below(i);
                if (!CEEBlocks.VOLTAGE_REGULATOR.has(level.getBlockState(pos))) {
                    BlockPos bottomPos = worldPosition.below(i - 1);
                    if (level.getBlockEntity(bottomPos) instanceof VoltageRegulatorBlockEntity be)
                        return be.addToGoggleTooltip(tooltip, isPlayerSneaking);
                    return false;
                }
            }
        }

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.electric_stats")
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.input_voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(Math.abs(inputVoltage))))
                .translate("generic.volts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.output_voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(Math.abs(outputVoltage))))
                .translate("generic.volts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.power")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(power)))
                .translate("generic.watts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide)
            if (Math.abs(lastSentPower - power) > 100) {
                lastSentPower = power;
                sendData();
            }

        if (!level.isClientSide)
            return;

        CatnipServices.PLATFORM.executeOnClientOnly(() -> this::tickAudio);
    }

    @Override
    public void lazyTick() {
        if (level.isClientSide)
            return;
        lastSentPower = power;
        sendData();
    }

    @OnlyIn(Dist.CLIENT)
    protected void tickAudio() {
        if (power > 100) {
            if (soundInstance == null || soundInstance.isStopped())
                Minecraft.getInstance()
                        .getSoundManager()
                        .play(soundInstance = new ElectricHumSoundInstance(worldPosition));
            else if (soundInstance != null) {
                soundInstance.setVolume((float) Mth.clamp(power / 800000, 0.02, 0.25));
                soundInstance.keepAlive();
            }
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

        voltage = new VoltageScrollValueBehaviour(CEELang.translate("voltage_regulator.voltage").component(), this, new ValueBox());

        voltage.withCallback(i -> this.updateVoltage());
        behaviours.add(voltage);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (clientPacket) {
            tag.putDouble("P", power);
            tag.putDouble("I", inputVoltage);
            tag.putDouble("O", outputVoltage);
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (clientPacket) {
            power = tag.getDouble("P");
            inputVoltage = tag.getDouble("I");
            outputVoltage = tag.getDouble("O");
        }
    }

    private void updateVoltage() {
        if (!(level instanceof ServerLevel sl))
            return;
        VoltageRegulatorDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, VoltageRegulatorDevice.class);

        if (device != null) {
            device.targetVoltage = voltage.getVoltage();
        }

        if (!getBlockState().getValue(VoltageRegulatorBlock.BOTTOM))
            if (level.getBlockEntity(worldPosition.below()) instanceof VoltageRegulatorBlockEntity be)
                be.voltage.setValue(voltage.value);
    }

    static class ValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 10, 13);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return super.getLocalOffset(level, pos, state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return state.getValue(VoltageRegulatorBlock.TOP) &&  direction.getAxis().isHorizontal() && direction.getAxis() == state.getValue(VoltageRegulatorBlock.FACING).getAxis();
        }
    }
}
