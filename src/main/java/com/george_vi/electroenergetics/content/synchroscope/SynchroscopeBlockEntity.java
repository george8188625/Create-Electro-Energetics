package com.george_vi.electroenergetics.content.synchroscope;

import com.george_vi.electroenergetics.foundation.CEELang;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.lang.FontHelper;
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

public class SynchroscopeBlockEntity extends SmartBlockEntity implements IHaveHoveringInformation {
    float phaseOffset = 0;
    float prevPhaseOffset = 0;
    int tickLength = 0;
    int counter = 0;
    public boolean validConnection;
    LerpedFloat smoothPhase = LerpedFloat.angular();

    public SynchroscopeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
            prevPhaseOffset = phaseOffset;
        smoothPhase.tickChaser();
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        if (clientPacket) {
            prevPhaseOffset = phaseOffset;
            tickLength = Math.max(1, counter);
            counter = 0;
        }
        phaseOffset = tag.getFloat("PhaseOffset");
        validConnection = tag.getBoolean("ValidConnection");

        float diff = (AngleHelper.getShortestAngleDiff(phaseOffset, prevPhaseOffset) % 180) / 180f;
        smoothPhase.chase(phaseOffset, diff, LerpedFloat.Chaser.EXP);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("PhaseOffset", phaseOffset);
        tag.putBoolean("ValidConnection", validConnection);
    }

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (validConnection)
            return false;
        CEELang.translate("hint.synchroscope_misconfigured.title")
                .style(ChatFormatting.GOLD)
                .forGoggles(tooltip);
        Component hint = CEELang.translateDirect("hint.synchroscope_misconfigured");
        List<Component> cutComponent = TooltipHelper.cutTextComponent(hint, FontHelper.Palette.GRAY_AND_WHITE);
        for (Component component : cutComponent)
            CreateLang.builder().add(component).forGoggles(tooltip);
        return true;
    }
}
