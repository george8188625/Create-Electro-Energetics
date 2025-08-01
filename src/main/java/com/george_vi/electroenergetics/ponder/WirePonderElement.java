package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.simulation.WireType;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.foundation.element.AnimatedSceneElementBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class WirePonderElement extends AnimatedSceneElementBase {
    final Vec3 pos1, pos2;
    final WireType type;

    public WirePonderElement(Vec3 pos1, Vec3 pos2, WireType type) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.type = type;
    }

    @Override
    protected void renderLast(PonderLevel world, MultiBufferSource buffer, GuiGraphics graphics, float fade, float pt) {
        List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, 1);
        for (int i = 0; i < points.size(); i++) {
            Vec3 point = points.get(i);
            Vec3 nextPoint = i == points.size() - 1 ? pos2 : points.get(i + 1);
            CachedBuffers.partial(type.getModel(), Blocks.ANDESITE.defaultBlockState())
                    .translate(point)
                    .rotateY((float) Math.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                    .rotateX(-(float) Math.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                    .scaleZ((float) (point.distanceTo(nextPoint) * 2))
                    .light(LevelRenderer.getLightColor(world, BlockPos.containing(point.add(nextPoint).multiply(0.5, 0.5, 0.5))))
                    .renderInto(graphics.pose(), buffer.getBuffer(RenderType.SOLID));
        }
    }
}
