package com.george_vi.electroenergetics.content.resistive_heater;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ResistiveHeaterBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public float heat;
    public float voltage;
    public float prevSentHeat;
    public LerpedFloat smoothHeat = LerpedFloat.linear();

    public ResistiveHeaterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide) {
            smoothHeat.tickChaser();
            return;
        }

        BlazeBurnerBlock.HeatLevel heatLevel = calculateHeatLevel(heat);
        if (getBlockState().getValue(ResistiveHeaterBlock.HEAT_LEVEL) != heatLevel) {
            level.setBlockAndUpdate(worldPosition, getBlockState()
                    .setValue(ResistiveHeaterBlock.HEAT_LEVEL, heatLevel)
                    .setValue(ResistiveHeaterBlock.LIT, heatLevel != BlazeBurnerBlock.HeatLevel.NONE));
            notifyUpdate();
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("Heat", heat);
        if (!clientPacket)
            tag.putFloat("Voltage", voltage);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        heat = tag.getFloat("Heat");
        if (!clientPacket)
            voltage = tag.getFloat("Voltage");
        else
            smoothHeat.chase(heat, 0.002, LerpedFloat.Chaser.LINEAR);

    }

    public void setVoltage(float voltage) {
        this.voltage = voltage;
        float power = (float) (voltage * voltage / CEEConfigs.server().resistanceValues.resistiveHeaterResistance.get());


        heat += Mth.clamp(power / 16000, 0, 1) * 0.05f;
        heat *= 0.977f;
        heat = Mth.clamp(heat, 0, 1);
        if ((prevSentHeat < 0.05) != (heat < 0.05) ||
                Math.abs(prevSentHeat - heat) > 0.05) {
            sendData();
            prevSentHeat = heat;
        }
    }

    public BlazeBurnerBlock.HeatLevel calculateHeatLevel(float heat) {
        if (heat >= 0.6)
            return BlazeBurnerBlock.HeatLevel.KINDLED;
        if (heat >= 0.2)
            return BlazeBurnerBlock.HeatLevel.FADING;
        if (heat >= 0.1)
            return BlazeBurnerBlock.HeatLevel.SMOULDERING;
        return BlazeBurnerBlock.HeatLevel.NONE;
    }

}
