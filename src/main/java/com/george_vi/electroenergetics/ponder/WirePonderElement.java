package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.CEEPartialModels;
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
    final boolean catenary;

    public WirePonderElement(Vec3 pos1, Vec3 pos2, WireType type, boolean catenary) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.type = type;
        this.catenary = catenary;
    }

    @Override
    protected void renderLast(PonderLevel world, MultiBufferSource buffer, GuiGraphics graphics, float fade, float pt) {
        if (catenary) {
            List<Vec3> lowerWirePoints = QuadraticWireHelper.cablePoints(pos1, pos2, 0, 10);

            for (int i = 0; i < lowerWirePoints.size(); i++) {
                Vec3 point = lowerWirePoints.get(i);
                Vec3 nextPoint = i == lowerWirePoints.size() - 1 ? pos2 : lowerWirePoints.get(i + 1);

                CachedBuffers.partial(CEEPartialModels.WIRE_SEGMENT, Blocks.ANDESITE.defaultBlockState())
                        .translate(point)
                        .rotateY((float) Math.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                        .rotateX(-(float) Math.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                        .scaleZ((float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                        .light(BlockPos.containing(point).equals(BlockPos.containing(nextPoint)) ? LevelRenderer.getLightColor(world, BlockPos.containing(point.add(nextPoint).multiply(0.5, 0.5, 0.5))) :
                                Math.max(LevelRenderer.getLightColor(world, BlockPos.containing(point)),
                                        LevelRenderer.getLightColor(world, BlockPos.containing(nextPoint))))
                        .renderInto(graphics.pose(), buffer.getBuffer(RenderType.solid()));
            }


            Vec3 topStart = pos1.add(0, 1, 0);
            Vec3 topEnd = pos2.add(0, 1, 0);

            float distance = (float) pos1.distanceTo(topEnd);


            List<Vec3> upperWirePoints = QuadraticWireHelper.cablePoints(topStart, topEnd, 200f * (0.05f / distance), 3);

            for (int i = 0; i < upperWirePoints.size(); i++) {
                Vec3 point = upperWirePoints.get(i);
                Vec3 nextPoint = i == upperWirePoints.size() - 1 ? topEnd : upperWirePoints.get(i + 1);

                CachedBuffers.partial(CEEPartialModels.WIRE_SEGMENT, Blocks.ANDESITE.defaultBlockState())
                        .translate(point)
                        .rotateY((float) Math.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                        .rotateX(-(float) Math.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                        .scaleZ((float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                        .light(BlockPos.containing(point).equals(BlockPos.containing(nextPoint)) ? LevelRenderer.getLightColor(world, BlockPos.containing(point.add(nextPoint).multiply(0.5, 0.5, 0.5))) :
                                Math.max(LevelRenderer.getLightColor(world, BlockPos.containing(point)),
                                        LevelRenderer.getLightColor(world, BlockPos.containing(nextPoint))))
                        .renderInto(graphics.pose(), buffer.getBuffer(RenderType.solid()));

                Vec3 closest;
                Vec3 ab = pos2.subtract(pos1);
                Vec3 ap = point.subtract(pos1);
                double denom = ab.lengthSqr();
                if (denom == 0)
                    closest = pos1;
                else {
                    double t = ap.dot(ab) / denom;
                    t = Math.max(0, Math.min(1, t));
                    closest = pos1.add(ab.scale(t));
                }

                CachedBuffers.partial(CEEPartialModels.WIRE_SEGMENT, Blocks.ANDESITE.defaultBlockState())
                        .translate(point)
                        .rotateY((float) Math.atan2(closest.x() - point.x(), closest.z() - point.z()))
                        .rotateX(-(float) Math.atan2(closest.y - point.y, Math.hypot(closest.x - point.x, closest.z - point.z)))
                        .scaleZ((float) (point.distanceTo(closest) * 2) + 0.02f)
                        .light(BlockPos.containing(point).equals(BlockPos.containing(closest)) ? LevelRenderer.getLightColor(world, BlockPos.containing(point.add(closest).multiply(0.5, 0.5, 0.5))) :
                                Math.max(LevelRenderer.getLightColor(world, BlockPos.containing(point)),
                                        LevelRenderer.getLightColor(world, BlockPos.containing(closest))))
                        .renderInto(graphics.pose(), buffer.getBuffer(RenderType.solid()));
            }
            return;
        }

        List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, type.getSag());
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
