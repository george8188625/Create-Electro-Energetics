package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.foundation.element.AnimatedSceneElementBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CurrentVisualizationPonderElement extends AnimatedSceneElementBase {
    final InWorldNode node1, node2;
    final float speed;
    final float sag;
    final boolean valid;

    public CurrentVisualizationPonderElement(InWorldNode node1, InWorldNode node2, float speed, float sag, boolean valid) {
        this.node1 = node1;
        this.node2 = node2;
        this.speed = speed;
        this.sag = sag;
        this.valid = valid;
    }

    @Override
    protected void renderLast(PonderLevel world, MultiBufferSource buffer, GuiGraphics graphics, float fade, float pt) {
        Vec3 pos1 = node1.getPosition(world);
        Vec3 pos2 = node2.getPosition(world);
        float progress = speed == 0 ? 0 : (AnimationTickHolder.getRenderTime(world) % (1 / (speed / 10))) * (speed / 10);
        if (speed < 0)
            progress = 1 - Math.abs(progress);
        List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, sag);
        for (int i = 0; i < points.size(); i++) {
            Vec3 point = points.get(i);
            Vec3 nextPoint = i == points.size() - 1 ? pos2 : points.get(i + 1);
            Vec3 position = VecHelper.lerp(progress, point, nextPoint);

            CachedBuffers.block(valid ? Blocks.YELLOW_CONCRETE.defaultBlockState() : Blocks.RED_CONCRETE.defaultBlockState())
                    .translate(position)
                    .scale(0.1875f + 0.0001f)
                    .uncenter()
                    .disableDiffuse()
                    .light(LightTexture.FULL_BRIGHT)
                    .renderInto(graphics.pose(), buffer.getBuffer(RenderType.solid()));
        }
    }
}
