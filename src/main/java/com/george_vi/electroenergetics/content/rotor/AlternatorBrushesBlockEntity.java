package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.content.wire.WireRenderer;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.foundation.InWorldNode;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangNumberFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class AlternatorBrushesBlockEntity extends KineticBlockEntity {

    List<AlternatorRotorBlockEntity> rotors = new ArrayList<>();
    public AlternatorBrushesBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (rotors.size() < 16)
            getRotors();
        for (int i = 0; i < rotors.size(); i++) {
            AlternatorRotorBlockEntity rotor = rotors.get(i);
            if (rotor.isRemoved()) {
                getRotors();
                break;
            }
        }

        float totalStress = 0;
        int magnets = 0;
        for (int i = 0; i < rotors.size(); i++) {
            AlternatorRotorBlockEntity rotor = rotors.get(i);
            totalStress += rotor.magnets * 48 * Math.abs(rotor.getSpeed());
            magnets += rotor.magnets;
        }

        if (!(level instanceof ServerLevel sl))
            return;

        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        InfrastructureSavedData.SimulatedDeviceInstance deviceInstance = sd.getDevice(worldPosition);

        if (deviceInstance == null)
            return;

        deviceInstance.extraData().putDouble("Stress", totalStress);
        deviceInstance.extraData().putDouble("Voltage", totalStress / 100);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        float totalStress = 0;
        int magnets = 0;
        for (int i = 0; i < rotors.size(); i++) {
            AlternatorRotorBlockEntity rotor = rotors.get(i);
            totalStress += rotor.magnets * 48 * Math.abs(rotor.getSpeed());
            magnets += rotor.magnets;
        }

        Double v1 = WireRenderer.getAllVoltages().get(new InWorldNode(0, getBlockPos()));
        Double v2 = WireRenderer.getAllVoltages().get(new InWorldNode(1, getBlockPos()));
        if (v1 == null || v2 == null)
            return false;
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
                .text(LangNumberFormat.format(Math.round(Math.abs(v1 - v2))))
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
            if (!(level.getBlockEntity(worldPosition.relative(getBlockState().getValue(AlternatorBrushesBlock.FACING).getOpposite(), i + 1)) instanceof AlternatorRotorBlockEntity be && be.getAxis() == getBlockState().getValue(AlternatorBrushesBlock.FACING).getAxis()))
                break;
            rotors.add(be);
        }
        this.rotors = rotors;
    }
}
