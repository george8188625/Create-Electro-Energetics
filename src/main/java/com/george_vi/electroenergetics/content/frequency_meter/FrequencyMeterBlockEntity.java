package com.george_vi.electroenergetics.content.frequency_meter;

import com.george_vi.electroenergetics.foundation.CEELang;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class FrequencyMeterBlockEntity extends SmartBlockEntity implements IHaveHoveringInformation {
    float frequency = 0;
    float prevFrequency = 0;
    int tickLength = 0;
    int counter = 0;
    LerpedFloat smoothFrequency = LerpedFloat.angular();

    public FrequencyMeterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public void tick() {
        super.tick();
        counter = Mth.clamp(counter + 1, 0, 20);
        if (counter > 20)
            prevFrequency = frequency;
        if (AngleHelper.getShortestAngleDiff(smoothFrequency.getValue(), frequency) > 1)
            smoothFrequency.chase(frequency, 0.1f, LerpedFloat.Chaser.EXP);
        smoothFrequency.tickChaser();
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        if (clientPacket) {
            prevFrequency = frequency;
            tickLength = Math.max(1, counter);
            counter = 0;
        }
        frequency = tag.getFloat("Frequency");

        float diff = (AngleHelper.getShortestAngleDiff(frequency, prevFrequency) % 180) / 180f;
        if (Float.isNaN(smoothFrequency.getValue()))
            smoothFrequency.setValue(0);
        smoothFrequency.chase(frequency, Mth.lerp(diff, 0.1, 1), LerpedFloat.Chaser.EXP);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("Frequency", frequency);
    }

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("gui.gauge.info_header")
                .forGoggles(tooltip);

        CEELang.builder()
                .translate("generic.frequency")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        float v = Math.round(frequency * 100) / 100f;

        CEELang.builder()
                .add(CreateLang.number(v))
                .add(Component.translatable("electroenergetics.generic.hertz"))
                .style(v < 5 ? ChatFormatting.DARK_GRAY : ChatFormatting.AQUA)
                .forGoggles(tooltip);

        return true;
    }
}
