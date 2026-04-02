package com.george_vi.electroenergetics.foundation.scroll_value;

import com.george_vi.electroenergetics.foundation.CEELang;
import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class ScalingScrollValueBehaviour extends ScrollValueBehaviour {

    private static final double[] scales = new double[]{1/1000d, 1/100d, 1/10d, 1d, 10d, 100d, 1000d};
    private static final int[] displayScales = new int[]{1000, 100, 10, 1, 10, 100, 1000};

    public ScalingScrollValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
        super(label, be, slot);
        this.between(0, 7);
        this.value = 3;
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(label, 6, 1, ImmutableList.of(CEELang.translate("gauge.scaling_symbol").component()),
                new ValueSettingsFormatter(valueSettings ->
                        CEELang.translateDirect(valueSettings.value() > 2 ? "gauge.scaling_forward" : "gauge.scaling_backward").append(String.valueOf(displayScales[Mth.clamp(valueSettings.value(), 0, scales.length - 1)]))));
    }

    @Override
    public String formatValue() {
        return CEELang.string(value > 2 ? "gauge.scaling_forward" : "gauge.scaling_backward") + displayScales[Mth.clamp(value, 0, scales.length - 1)];
    }

    // Overwrite so that when people update, it doesn't turn to 1/1000.
    @Override
    public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(nbt, registries, clientPacket);
        value = nbt.contains("ScrollValue") ? nbt.getInt("ScrollValue") : 3;
    }

    public double getScale() {
        return scales[Mth.clamp(value, 0, scales.length - 1)];
    }
}
