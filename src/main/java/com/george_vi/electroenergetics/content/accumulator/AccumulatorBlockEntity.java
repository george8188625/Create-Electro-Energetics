package com.george_vi.electroenergetics.content.accumulator;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.electrical_properties.AccumulatorProperties;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AccumulatorBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public double cell1Charge = 0;
    public double cell2Charge = 0;
    public boolean isDoubleCell = false;

    public AccumulatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(8);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        sendData();
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putDouble("Cell1Charge", cell1Charge);
        tag.putDouble("Cell2Charge", cell2Charge);
        if (isDoubleCell)
            tag.putBoolean("IsDoubleCell", true);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        cell1Charge = tag.getFloat("Cell1Charge");
        cell2Charge = tag.getFloat("Cell2Charge");
        isDoubleCell = tag.getBoolean("IsDoubleCell");
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Minecraft mc = Minecraft.getInstance();
        HitResult hitResult = mc.hitResult;
        if (hitResult == null)
            return false;

        BlockState state = getBlockState();
        Direction facing = state.getValue(AccumulatorBlock.FACING);
        boolean roll = state.getValue(AccumulatorBlock.ROLL);

        CEELang.builder()
                .translate("gui.goggles.electric_stats")
                .forGoggles(tooltip);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.stored_energy")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        boolean clickedOnRight = AccumulatorBlock.clickedOnFirst(roll, facing,
                hitResult.getLocation().subtract(Vec3.atLowerCornerOf(worldPosition)));

        boolean isFirst = true;
        if (state.getValue(AccumulatorBlock.STACK).isDouble())
            isFirst = clickedOnRight ^ roll;

        double charge = isFirst ? cell1Charge : cell2Charge;

        CEELang.builder()
                .add(CEELang.formatEnergy(AccumulatorProperties.energyFromSOC(charge, 24, AccumulatorProperties.getNominalCharge())))
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.state_of_charge")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        CEELang.builder()
                .add(CreateLang.number(charge / AccumulatorProperties.getNominalCharge() * 100d))
                .add(Component.literal("%"))
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);
        return true;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

}
