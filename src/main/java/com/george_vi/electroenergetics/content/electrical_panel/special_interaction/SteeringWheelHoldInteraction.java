package com.george_vi.electroenergetics.content.electrical_panel.special_interaction;

import com.george_vi.electroenergetics.client.ElectricPropertiesOverlay;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.foundation.HoldInteractionBehavior;
import com.simibubi.create.AllSoundEvents;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class SteeringWheelHoldInteraction implements HoldInteractionBehavior {
    public final BlockPos pos;
    public final int panelSlot;
    public int redstoneSignal;
    public float analogSignal;

    public SteeringWheelHoldInteraction(BlockPos pos, int panelSlot, int redstoneSignal) {
        this.pos = pos;
        this.panelSlot = panelSlot;
        this.redstoneSignal = redstoneSignal;
        this.analogSignal = redstoneSignal;
    }

    @Override
    public void release() {
        ElectricPropertiesOverlay.INSTANCE.removeAnalogLever();
    }

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null)
            return;

        int prevRedstoneSignal = redstoneSignal;
        redstoneSignal = Math.round(analogSignal);
        redstoneSignal = Mth.clamp(redstoneSignal, 0, 15);
        if (prevRedstoneSignal != redstoneSignal) {
            mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    AllSoundEvents.SCROLL_VALUE.getMainEvent(), SoundSource.PLAYERS,
                    0.25f, 1f, false);
            CatnipServices.NETWORK.sendToServer(new AnalogPanelAttachmentChangeStatePacket(pos, panelSlot, (byte) redstoneSignal));
        }

        ElectricPropertiesOverlay.INSTANCE.setAnalogLever(redstoneSignal);
    }

    @Override
    public void onMouseMove(double y, double x) {
        analogSignal = Mth.clamp(analogSignal + (float) x * 0.05f, 0, 15);
    }

    @Override
    public boolean isStillActive() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return false;
        double range = mc.player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;

        if (mc.player.distanceToSqr(pos.getCenter()) > range * range)
            return false;

        if (mc.level != null && mc.level.getBlockEntity(pos) instanceof ElectricalPanelBlockEntity be) {
            return be.getAttachments()[panelSlot] instanceof SteeringWheelPanelAttachment;
        }
        return false;
    }
}
