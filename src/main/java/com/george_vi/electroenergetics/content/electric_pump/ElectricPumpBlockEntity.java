package com.george_vi.electroenergetics.content.electric_pump;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.wire.WireRenderer;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
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
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class ElectricPumpBlockEntity extends PumpBlockEntity {
    float voltage = 0;

    List<Float> voltages = new ArrayList<>();
    float avgVoltage = 0;

    public ElectricPumpBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide) {
            Double v1 = WireRenderer.NODE_VOLTAGES.get(new InWorldNode(0, getBlockPos()));
            Double v2 = WireRenderer.NODE_VOLTAGES.get(new InWorldNode(1, getBlockPos()));
            if (v1 == null || v2 == null)
                return;
            setVoltage((float) (v1 - v2));
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
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
        voltage = tag.getFloat("Voltage");
        voltages = NBTHelper.readCompoundList(tag.getList("Voltages", Tag.TAG_COMPOUND), t -> t.getFloat("V"));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (Math.abs(avgVoltage) < 0.1)
            return false;
        CEELang.builder()
                .translate("gui.goggles.energy_consumption")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CEELang.builder()
                .text(LangNumberFormat.format(Math.round(avgVoltage * avgVoltage / CEEConfigs.server().resistanceValues.pumpResistance.get())))
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
        return (float) Mth.clamp(avgVoltage * avgVoltage / CEEConfigs.server().resistanceValues.pumpResistance.get() / AllConfigs.server().kinetics.stressValues.getImpact(AllBlocks.MECHANICAL_PUMP.get()).getAsDouble(), -300, 300);
    }

    public void setVoltage(float voltage) {
        if (voltages.size() >= 30)
            voltages.remove(0);
        voltages.add(voltage);
        avgVoltage = voltages.stream().reduce(Float::sum).orElse(0f) / voltages.size();

        if (Math.abs(voltage) == Math.abs(this.voltage))
            return;
        this.voltage = voltage;
        if (speed != 0)
            award(AllAdvancements.PUMP);
        if (level.isClientSide && !isVirtual())
            return;

        updatePressureChange();
    }

    public double getResistance() {
        return CEEConfigs.server().resistanceValues.pumpResistance.get();
    }
}
