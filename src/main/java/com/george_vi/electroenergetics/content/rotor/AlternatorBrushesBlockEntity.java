package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangNumberFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class AlternatorBrushesBlockEntity extends KineticBlockEntity {

    List<AlternatorRotorBlockEntity> rotors = new ArrayList<>();
    BlockPos otherBrush = null;
    float voltage;
    public AlternatorBrushesBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (rotors.size() < 16)
            getRotors();
        for (AlternatorRotorBlockEntity rotor : rotors) {
            if (rotor.isRemoved()) {
                getRotors();
                break;
            }
        }

        float totalStress = 0;
        for (AlternatorRotorBlockEntity rotor : rotors) {
            totalStress += rotor.magnets * 48 * Math.abs(rotor.getSpeed());
        }

        if (!(level instanceof ServerLevel sl))
            return;

        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        SimulatedDeviceInstance<?> deviceInstance = sd.getDevice(worldPosition);

        if (deviceInstance == null || !(deviceInstance.extraData() instanceof AlternatorBrushesDevice.DataHolder dataHolder))
            return;

        dataHolder.stress = totalStress;
        dataHolder.voltage = totalStress / 100;
        dataHolder.otherBrush = otherBrush;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        float totalStress = 0;
        for (int i = 0; i < rotors.size(); i++) {
            AlternatorRotorBlockEntity rotor = rotors.get(i);
            totalStress += rotor.magnets * 48 * Math.abs(rotor.getSpeed());
        }

        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate("gui.goggles.electric_stats")
                .forGoggles(tooltip);

        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate("gui.goggles.energy_generation")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        Lang.builder(CreateElecrtoEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(Math.abs(totalStress))))
                .translate("generic.watts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate("gui.goggles.voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        Lang.builder(CreateElecrtoEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(voltage)))
                .translate("generic.volts")
                .text(" / " + LangNumberFormat.format(Math.round(totalStress / 100)))
                .translate("generic.volts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);
        return true;
    }

    public void getRotors() {
        List<AlternatorRotorBlockEntity> rotors = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            BlockPos rotorPos = worldPosition.relative(getBlockState().getValue(AlternatorBrushesBlock.FACING).getOpposite(), i + 1);
            BlockEntity rawBE = level.getBlockEntity(rotorPos);
            if (rawBE instanceof AlternatorBrushesBlockEntity be && be.getBlockState().getValue(AlternatorBrushesBlock.FACING) == getBlockState().getValue(AlternatorBrushesBlock.FACING).getOpposite()) {
                otherBrush = rotorPos;
                break;
            }
            if (!(rawBE instanceof AlternatorRotorBlockEntity be && be.getAxis() == getBlockState().getValue(AlternatorBrushesBlock.FACING).getAxis())) {
                otherBrush = null;
                break;
            }
            rotors.add(be);
        }
        this.rotors = rotors;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (clientPacket)
            tag.putFloat("V", voltage);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (clientPacket)
            voltage = tag.getFloat("V");
    }
}
