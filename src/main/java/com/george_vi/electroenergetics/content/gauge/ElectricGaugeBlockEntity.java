package com.george_vi.electroenergetics.content.gauge;

import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.content.wire_spool.WireRenderer;
import com.george_vi.electroenergetics.foundation.Node;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ElectricGaugeBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public float dialTarget;
    public float dialState;
    public float prevDialState;
    public int color;
    public final boolean voltmeter;
    public double voltage;

    ElectricGaugeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, boolean voltmeter) {
        super(type, pos, state);
        this.voltmeter = voltmeter;
    }

    public static ElectricGaugeBlockEntity voltmeter(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new ElectricGaugeBlockEntity(type, pos, state, true);
    }

    public static ElectricGaugeBlockEntity ammeter(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new ElectricGaugeBlockEntity(type, pos, state, false);
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        compound.putFloat("Value", dialTarget);
        compound.putInt("Color", color);
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        dialTarget = compound.getFloat("Value");
        color = compound.getInt("Color");
        super.read(compound, registries, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide()) {
            Float v1 = WireRenderer.getAllVoltages().get(new Node(0, getBlockPos()));
            Float v2 = WireRenderer.getAllVoltages().get(new Node(1, getBlockPos()));

            if (v1 != null && v2 != null)
                setValue(voltmeter ? Math.abs(v1 - v2) : Math.abs(v1 - v2) / 0.01);

            prevDialState = dialState;
            dialState += (dialTarget - dialState) * .125f;
            if (dialState > 1 && level.random.nextFloat() < 1 / 2f)
                dialState -= (dialState - 1) * level.random.nextFloat();
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("gui.gauge.info_header")
                .forGoggles(tooltip);
        Float v1 = WireRenderer.getAllVoltages().get(new Node(0, getBlockPos()));
        Float v2 = WireRenderer.getAllVoltages().get(new Node(1, getBlockPos()));
        if (v1 == null || v2 == null) {
            v1 = 0f;
            v2 = 0f;
        }

        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate(voltmeter ? "generic.voltage" : "generic.current")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        float v = Math.round(Math.abs(v1 - v2) / (voltmeter ? 1 : 0.01));
        if (v <= 1)
            v = (float) (Math.abs(v1 - v2) / (voltmeter ? 1 : 0.01));
        Lang.builder(CreateElecrtoEnergetics.ID)
                .text(TooltipHelper.makeProgressBar(3, dialState < 0.01 ? 0 : dialState < 0.33 ? 1 : dialState < 0.66 ? 2 : 3))
                .space()
                .add(CreateLang.number(v))
                .add(Component.literal(voltmeter ? "V" : "A"))
                .style(dialState < 0.01 ? ChatFormatting.DARK_GRAY :
                        dialState < 0.33f ? ChatFormatting.GREEN :
                        dialState < 0.66f ? (voltmeter ? ChatFormatting.AQUA : ChatFormatting.GOLD) :
                                (voltmeter ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.RED))
                .forGoggles(tooltip);
        return true;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    public void setValue(double v) {
        dialTarget = (float) Mth.clamp(voltmeter ? v / 1000 : v / 40, 0, 1);
    }
}
