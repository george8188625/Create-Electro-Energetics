package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class DetachedNodeRenderer {
    @OnlyIn(Dist.CLIENT)
    public static void render(ClientLevel level, PoseStack pose, MultiBufferSource buffer, Camera camera) {
        Minecraft mc = Minecraft.getInstance();

        pose.pushPose();
        pose.translate(-camera.getPosition().x(), -camera.getPosition().y(), -camera.getPosition().z());

        for (ClientNodeData nodeData : WireRenderer.NODE_DATA.values()) {
            Vec3 pos = nodeData.getPos(AnimationTickHolder.getPartialTicks());
            if (pos == null)
                continue;
            CachedBuffers.partial(CEEPartialModels.WIRE_TIE, Blocks.ANDESITE.defaultBlockState())
                    .translate(pos)
                    .light(LevelRenderer.getLightColor(level, BlockPos.containing(pos)))
                    .renderInto(pose, buffer.getBuffer(RenderType.cutout()));
        }

        pose.popPose();
    }
}
