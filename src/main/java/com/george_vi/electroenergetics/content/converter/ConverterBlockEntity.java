package com.george_vi.electroenergetics.content.converter;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.google.common.collect.ImmutableList;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.EnergyStorage;

import java.util.List;

public class ConverterBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    SimulatedDeviceInstance<?> converterDevice = null;

    public ConverterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected ScrollValueBehaviour voltage;
    double power;
    double storedEnergy;

    ConverterEnergyStorage capability = new ConverterEnergyStorage(this);

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        voltage = new ScrollValueBehaviour(Component.translatable("electroenergetics.converter.voltage"),
                this, new ValueBox()) {
            @Override
            public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
                return new ValueSettingsBoard(
                        label, max, 10,
                        ImmutableList.of(Component.translatable("electroenergetics.converter.voltage_symbol")),
                        new ValueSettingsFormatter(valueSettings ->
                                CEELang.formatVoltage(valueSettings.value() * 10).component()));
            }
        };
        voltage.between(0, 432);
        voltage.value = 10;
        voltage.withFormatter(v -> CEELang.formatVoltage(v * 10).string());
        voltage.withCallback(i -> this.updateVoltage());
        behaviours.add(voltage);
    }

    private void updateVoltage() {
        if (!(level instanceof ServerLevel sl))
            return;
        if (converterDevice == null || !converterDevice.isValid()) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            converterDevice = sd.getDevice(getBlockPos());
        }

        if (converterDevice != null && converterDevice.extraData() instanceof ConverterDevice.DataHolder dataHolder) {
            dataHolder.voltage = voltage.value * 10;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level instanceof ServerLevel sl))
            return;

        if (converterDevice == null || !converterDevice.isValid()) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            converterDevice = sd.getDevice(getBlockPos());
        }
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                CEEBlockEntityTypes.CONVERTER.get(),
                (be, context) ->
                        context == null || be.getBlockState().getValue(ConverterBlock.FACING).getOpposite() == context ? be.capability : null);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (Math.abs(power) <= 1)
            return false;
        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.energy_consumption")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .add(power < 0 ? CEELang.formatPower(Math.abs(power)) :
                        CEELang.formatFEPerTick(Math.abs(power / CEEConfigs.server().wattFeTConversionRate.get())))
                .style(ChatFormatting.AQUA)
                .space()
                .add(Component.translatable("electroenergetics.gui.goggles.at_current_load")
                        .withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.energy_generation")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .add(power > 0 ? CEELang.formatPower(Math.abs(power)) :
                        CEELang.formatFEPerTick(Math.abs(power / CEEConfigs.server().wattFeTConversionRate.get())))
                .style(ChatFormatting.AQUA)
                .space()
                .add(Component.translatable("electroenergetics.gui.goggles.at_current_load")
                        .withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);
        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putDouble("Power", power);
        tag.putDouble("StoredEnergy", storedEnergy);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        power = tag.getDouble("Power");
        storedEnergy = tag.getDouble("StoredEnergy");
    }

    static class ValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 5);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return super.getLocalOffset(level, pos, state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis() == state.getValue(ConverterBlock.FACING).getAxis() &&
                    state.getValue(ConverterBlock.SOURCE);
        }
    }

    public static class ConverterEnergyStorage extends EnergyStorage {

        final ConverterBlockEntity be;

        public ConverterEnergyStorage(ConverterBlockEntity be) {
            super(Mth.floor(ConverterDevice.MAX_ENERGY / CEEConfigs.server().wattFeTConversionRate.get()));
            this.be = be;
        }

        @Override
        public int getEnergyStored() {
            if (be.converterDevice != null &&
                    be.converterDevice.extraData() instanceof ConverterDevice.DataHolder dataHolder)
                return Mth.floor(dataHolder.storedEnergy / CEEConfigs.server().wattFeTConversionRate.get());
            return be.level.isClientSide ? Mth.floor(be.storedEnergy / CEEConfigs.server().wattFeTConversionRate.get()) : 0;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            return 0;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
