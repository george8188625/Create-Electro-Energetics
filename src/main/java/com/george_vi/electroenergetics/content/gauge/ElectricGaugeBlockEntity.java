package com.george_vi.electroenergetics.content.gauge;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.compat.computercraft.CCProxy;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.ScalingScrollValueBehaviour;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.content.kinetics.gauge.GaugeBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
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
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.List;

public class ElectricGaugeBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public float dialTarget;
    public float dialState;
    public float prevDialState;
    public int color;
    public final boolean voltmeter;
    public double voltage;
    public AbstractComputerBehaviour computerBehaviour;
    public ScalingScrollValueBehaviour scaling;
    public int redstoneSignal;

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
        compound.putDouble("Voltage", voltage);
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        dialTarget = compound.getFloat("Value");
        color = compound.getInt("Color");
        voltage = compound.getDouble("Voltage");
        super.read(compound, registries, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide()) {
            prevDialState = dialState;
            dialState += (dialTarget - dialState) * .125f;
            if (dialState > 1 && level.random.nextFloat() < 1 / 2f)
                dialState -= (dialState - 1) * level.random.nextFloat();
        } else {
            int newRedstoneSignal = dialTarget < 0.001 ? 0 : Mth.ceil(Mth.clamp(dialTarget * 15, 0, 15));
            if (newRedstoneSignal != redstoneSignal) {
                redstoneSignal = newRedstoneSignal;
                setChanged();
            }
        }

    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("gui.gauge.info_header")
                .forGoggles(tooltip);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate(voltmeter ? "generic.voltage" : "generic.current")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        double v = Math.abs(voltmeter ? voltage : voltage / 0.01);
        v *= scaling.getScale();
        if (v  > 1)
            v = Math.round(v);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(TooltipHelper.makeProgressBar(3, dialState < 0.01 ? 0 : dialState < 0.33 ? 1 : dialState < 0.66 ? 2 : 3))
                .space()
                .add(CreateLang.number(v))
                .add(Component.translatable(voltmeter ? "electroenergetics.generic.volts" : "electroenergetics.generic.amps"))
                .style(dialState < 0.01 ? ChatFormatting.DARK_GRAY :
                        dialState < 0.33f ? ChatFormatting.GREEN :
                        dialState < 0.66f ? (voltmeter ? ChatFormatting.AQUA : ChatFormatting.GOLD) :
                                (voltmeter ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.RED))
                .forGoggles(tooltip);
        return true;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        scaling = new ScalingScrollValueBehaviour(CEELang.translateDirect("gauge.scaling"), this, new ValueBox());
        scaling.withCallback(i -> updateScale());
        behaviours.add(computerBehaviour = CCProxy.behaviour(this));
        behaviours.add(scaling);
    }

    private void updateScale() {

    }

    @Override
    public void invalidate() {
        super.invalidate();
        computerBehaviour.removePeripheral();
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        if (Mods.COMPUTERCRAFT.isLoaded()) {
            event.registerBlockEntity(
                    PeripheralCapability.get(),
                    CEEBlockEntityTypes.VOLTMETER.get(),
                    (be, context) -> be.computerBehaviour.getPeripheralCapability()
            );

            event.registerBlockEntity(
                    PeripheralCapability.get(),
                    CEEBlockEntityTypes.AMMETER.get(),
                    (be, context) -> be.computerBehaviour.getPeripheralCapability()
            );
        }
    }

    public void setValue(double v) {
        dialTarget = (float) Mth.clamp(voltmeter ? v / 1000 : v / 40, 0, 1);
        sendData();
    }

    static class ValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 14);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return super.getLocalOffset(level, pos, state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            Direction facing = state.getValue(ElectricGaugeBlock.FACING);
            boolean axisAlongFirst = state.getValue(GaugeBlock.AXIS_ALONG_FIRST_COORDINATE);
            if (facing.getAxis().isVertical())
                return direction == facing;
            if (facing.getAxis() == Direction.Axis.X)
                return axisAlongFirst ?
                        direction.getAxis() == Direction.Axis.Z :
                        direction.getAxis().isVertical();
            return axisAlongFirst ?
                    direction.getAxis().isVertical() :
                    direction.getAxis() == Direction.Axis.X;
        }
    }
}
