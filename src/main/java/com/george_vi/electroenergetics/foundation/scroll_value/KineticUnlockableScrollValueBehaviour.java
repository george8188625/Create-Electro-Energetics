package com.george_vi.electroenergetics.foundation.scroll_value;

import com.george_vi.electroenergetics.foundation.CEELang;
import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class KineticUnlockableScrollValueBehaviour extends ScrollValueBehaviour {

    public KineticUnlockableScrollValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
        super(label, be, slot);
        withFormatter(v -> isUnlocked() ?
                CEELang.translateDirect("electric_motor.x_rpm", getUnlockedStressRPMMultiplier(v)).getString() :
                String.valueOf(Math.abs(v)));
    }

    public boolean isUnlocked() {
        return value >= 1_000_000;
    }

    @Override
    public ScrollValueBehaviour between(int min, int max) {
        return super.between(min, 1_000_000 + max);
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        ImmutableList<Component> rows = ImmutableList.of(
                Component.literal("⟳")
                        .withStyle(ChatFormatting.BOLD),
                Component.literal("⟲")
                        .withStyle(ChatFormatting.BOLD),
                Component.literal("🔓")
                        .withStyle(ChatFormatting.BOLD));
        ValueSettingsFormatter formatter = new ValueSettingsFormatter(this::formatSettings);
        return new ValueSettingsBoard(label, 256, 32, rows, formatter);
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
        int value;
        if (valueSetting.row() == 2)
            value = 1_000_000 + Math.max(1, valueSetting.value());
        else
            value = Math.max(1, valueSetting.value());
        if (!valueSetting.equals(getValueSettings()))
            playFeedbackSound(this);
        setValue(valueSetting.row() == 0 ? -value : value);
    }

    @Override
    public ValueSettings getValueSettings() {
        if (value >= 1_000_000)
            return new ValueSettings(2, Math.abs(value));
        return new ValueSettings(value < 0 ? 0 : 1, Math.abs(value));
    }

    public MutableComponent formatSettings(ValueSettings settings) {
        if (settings.row() == 2)
            return CEELang.translateDirect("electric_motor.x_rpm", getUnlockedStressRPMMultiplier(settings.value()));
        return CreateLang.number(Math.max(1, Math.abs(settings.value())))
                .add(CreateLang.text(settings.row() == 0 ? "⟳" : "⟲")
                        .style(ChatFormatting.BOLD))
                .component();
    }

    public int getUnlockedStressRPMMultiplier(int value) {
        if (value < 1_000_000)
            return value;
        return value - 1_000_000;
    }

    public float getUnlockedStressRPMMultiplier() {
        return getUnlockedStressRPMMultiplier(value);
    }

    @Override
    public String getClipboardKey() {
        return "SpeedLockable";
    }
}
