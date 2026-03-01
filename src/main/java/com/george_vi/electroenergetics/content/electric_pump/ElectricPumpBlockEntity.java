package com.george_vi.electroenergetics.content.electric_pump;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.lang.LangNumberFormat;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class ElectricPumpBlockEntity extends PumpBlockEntity {
    float voltage = 0;


    public ElectricPumpBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("Voltage", voltage);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        voltage = tag.getFloat("Voltage");
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (Math.abs(voltage) < 0.1)
            return false;
        CEELang.builder()
                .translate("gui.goggles.energy_consumption")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CEELang.builder()
                .text(LangNumberFormat.format(Math.round(voltage * voltage / CEEConfigs.server().resistanceValues.pumpResistance.get())))
                .translate("generic.watts")
                .style(ChatFormatting.AQUA)
                .space()
                .add(Component.translatable("electroenergetics.gui.goggles.at_current_voltage")
                        .withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        CEELang.builder()
                .translate("gui.goggles.rpm_equivalent")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CreateLang.builder()
                .text(LangNumberFormat.format(Math.round(getSpeed())))
                .translate("generic.unit.rpm")
                .style(ChatFormatting.AQUA)
                .space()
                .add(Component.translatable("electroenergetics.gui.goggles.at_current_voltage")
                        .withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        return true;
    }

    @Override
    public float getSpeed() {
        return (float) Mth.clamp(voltage * voltage / CEEConfigs.server().resistanceValues.pumpResistance.get() / AllConfigs.server().kinetics.stressValues.getImpact(AllBlocks.MECHANICAL_PUMP.get()).getAsDouble(), -300, 300);
    }

    public void setVoltage(float voltage) {
        if (Math.abs(voltage - this.voltage) < 1)
            return;
        if (Math.abs(voltage) <= 1)
            voltage = 0;
        this.voltage = voltage;
        if (Math.abs(voltage) > 0.5)
            award(AllAdvancements.PUMP);
        if (level.isClientSide && !isVirtual())
            return;

        updatePressureChange();
    }

    public double getResistance() {
        return CEEConfigs.server().resistanceValues.pumpResistance.get();
    }
}
