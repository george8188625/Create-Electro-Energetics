package com.george_vi.electroenergetics.foundation;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class VoltageScrollValueBehaviour extends ScrollValueBehaviour {
    public VoltageScrollValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
        super(label, be, slot);
        this.between(0, 1_000_000_000);
        this.value = 300_000;
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(label, 190, 10, ImmutableList.of(CEELang.translate("creative_battery.voltage_symbol").component(),
                CEELang.translate("creative_battery.kilo_voltage_symbol").component()),
                new ValueSettingsFormatter(valueSettings -> CEELang.formatVoltage(indexToVoltage(valueSettings.value(), valueSettings.row())).component()));
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
        int value = Math.max(1, valueSetting.value());
        if (!valueSetting.equals(getValueSettings()))
            playFeedbackSound(this);
        setValue((int) (indexToVoltage(value, valueSetting.row()) * 1000d));
    }

    @Override
    public ValueSettings getValueSettings() {

        if (value <= 1_000_000) {
            if (value <= 100_000)
                return new ValueSettings(1, value / 1_000);
            else
                return new ValueSettings(1, (value / 10_000) + 90);
        }

        if (value <= 100_000_000)
            return new ValueSettings(2, value / 1_000_000);
        else
            return new ValueSettings(2, (value / 10_000_000) + 90);

    }

    @Override
    public String formatValue() {
        return CEELang.formatVoltage(value / 1000d).string();
    }

    public double getVoltage() {
        return value / 1000d;
    }

    private static double indexToVoltage(int i, int row) {
        if (row == 0) {

            if (i < 100)
                return i;
            else
                return (i - 90) * 10;
        }

        if (i < 100)
            return i == 0 ? 1000 : i * 1000;
        else
            return (i - 90) * 10000;
    }
}
