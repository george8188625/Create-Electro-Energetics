package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.bulb.BulbBlock;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.wire_spool.EmptySpoolItem;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.render.RenderTypes;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

public class IndicatorBulbPanelAttachment extends PanelAttachment {

    public final boolean miniature;
    public DyeColor color = DyeColor.WHITE;
    LerpedFloat smoothLight = LerpedFloat.linear();
    float light = 0f;

    private IndicatorBulbPanelAttachment(PanelAttachmentType type, boolean miniature) {
        super(type);
        this.miniature = miniature;
    }

    public static IndicatorBulbPanelAttachment normal(PanelAttachmentType type) {
        return new IndicatorBulbPanelAttachment(type, false);
    }

    public static IndicatorBulbPanelAttachment miniature(PanelAttachmentType type) {
        return new IndicatorBulbPanelAttachment(type, true);
    }

    @Override
    public void render(ElectricalPanelBlockEntity be, float partialTicks, PoseStack ms,
                       MultiBufferSource buffer, int light, int overlay) {
        transformPose(ms, be);

        CachedBuffers.partial(miniature ? CEEPartialModels.PANEL_ATTACHMENT_SMOL_INDICATOR_BULB : CEEPartialModels.PANEL_ATTACHMENT_INDICATOR_BULB, be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.SOLID));
        float lightStrength = smoothLight.getValue(partialTicks);

        RenderType renderType = lightStrength > 0.05f ? RenderTypes.additive() : RenderType.translucent();
        if (miniature) {
            CachedBuffers.partial(CEEPartialModels.INDICATOR_BULB_TUBE, be.getBlockState())
                    .color(color.getTextColor())
                    .rotateXCenteredDegrees(270)
                    .scale(3/6f, 2/6f, 3/6f)
                    .translate(4/16f, 16/16f, 8/16f)
                    .light(light)
                    .disableDiffuse()
                    .renderInto(ms, buffer.getBuffer(renderType));

            if (lightStrength > 0.05f) {
                int newColor = ((int)(((color.getMapColor().col >> 16) & 0xFF) * lightStrength) << 16)
                        | ((int)(((color.getMapColor().col >> 8) & 0xFF) * lightStrength) << 8)
                        | ((int)((color.getMapColor().col & 0xFF) * lightStrength));

                CachedBuffers.partial(CEEPartialModels.INDICATOR_BULB_GLOW, be.getBlockState())
                        .color(newColor)
                        .light(0xf000f0)
                        .disableDiffuse()
                        .translate(4/16f, 5/16f + 3/16f, 8/16f + 1/16f)
                        .scale(1f, 1f, 1.5f)
                        .translate(-4/16f, -5/16f, -8/16f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.additive()));

                int cubeColor = ((int)(((0xffffff >> 16) & 0xFF) * lightStrength) << 16)
                        | ((int)(((0xffffff >> 8) & 0xFF) * lightStrength) << 8)
                        | ((int)((0xffffff & 0xFF) * lightStrength));

                CachedBuffers.partial(CEEPartialModels.INDICATOR_BULB_CUBE, be.getBlockState())
                        .color(cubeColor)
                        .light(0xf000f0)
                        .disableDiffuse()
                        .translate(4/16f, 5/16f + 3/16f, 8/16f + 1/16f)
                        .scale(0.75f, 0.75f, 1f)
                        .translate(-4/16f, -5/16f, -8/16f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.additive()));
                }
        } else {

            CachedBuffers.partial(CEEPartialModels.INDICATOR_BULB_TUBE, be.getBlockState())
                    .color(color.getTextColor())
                    .rotateXCenteredDegrees(270)
                    .scale(4/6f, 2/6f, 4/6f)
                    .translate(3.5/16f, 16/16f, 4/16f)
                    .light(light)
                    .disableDiffuse()
                    .renderInto(ms, buffer.getBuffer(renderType));


            if (lightStrength > 0.05f) {
                int newColor = ((int)(((color.getMapColor().col >> 16) & 0xFF) * lightStrength) << 16)
                        | ((int)(((color.getMapColor().col >> 8) & 0xFF) * lightStrength) << 8)
                        | ((int)((color.getMapColor().col & 0xFF) * lightStrength));

                CachedBuffers.partial(CEEPartialModels.INDICATOR_BULB_GLOW, be.getBlockState())
                        .color(newColor)
                        .light(0xf000f0)
                        .disableDiffuse()
                        .translate(4/16f + 1/16f, 5/16f + 3/16f, 8/16f + 1/16f)
                        .scale(1.5f, 1.5f, 1.5f)
                        .translate(-4/16f, -5/16f, -8/16f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.additive()));

                int cubeColor = ((int)(((0xffffff >> 16) & 0xFF) * lightStrength) << 16)
                        | ((int)(((0xffffff >> 8) & 0xFF) * lightStrength) << 8)
                        | ((int)((0xffffff & 0xFF) * lightStrength));

                CachedBuffers.partial(CEEPartialModels.INDICATOR_BULB_CUBE, be.getBlockState())
                        .color(cubeColor)
                        .light(0xf000f0)
                        .disableDiffuse()
                        .translate(4/16f + 1/16f, 5/16f + 3/16f, 8/16f + 1/16f)
                        .scale(1f, 1f, 1f)
                        .translate(-4/16f, -5/16f, -8/16f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.additive()));
            }
        }
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        smoothLight.tickChaser();
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        bridges.bridge(nodes[0], nodes[1], ElectricalProperties.resistor(1000));
    }

    @Override
    public void postTick(SimulationResults results) {
        double voltage = results.getVoltageAt(nodes[0], nodes[1]);
        float newLight = 0;
        newLight = (float) Math.min(1, Math.abs(results.getVoltageAt(nodes[0], nodes[1]) / 70));

        if (Math.abs(light - newLight) > 0.02) {
            light = newLight;
            sendData();
        }
    }

    @Override
    public ItemInteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof WireSpoolItem ||
                stack.getItem() instanceof EmptySpoolItem ||
                AllItems.WRENCH.isIn(stack))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (stack.getItem() instanceof DyeItem di) {
            color = di.getDyeColor();
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS);
            sendData();
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket) {
        color = DyeColor.byName(tag.getString("Color"), DyeColor.WHITE);
        light = tag.getFloat("Light");

        if (clientPacket)
            smoothLight.chase(light, 0.75, LerpedFloat.Chaser.EXP);
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket) {
        if (light != 0)
            tag.putFloat("Light", light);
        if (color != null)
            tag.putString("Color", color.getSerializedName());
    }
}
