package com.george_vi.electroenergetics.content.railway_electrification.third_rail;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.TrainPantographEntry;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.mixin_interfaces.IPantographList;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class RailContactShoeMovementBehaviour implements MovementBehaviour {
    @Override
    public void tick(MovementContext context) {
        if (!(context.contraption.entity instanceof CarriageContraptionEntity e))
            return;

        boolean prevDisabled = context.data.getBoolean("Disabled");
        if (!context.data.contains("Forward")) {
            TrainPantographEntry pantographState = ((IPantographList) e.getCarriage()).getPantographState(context.localPos);
            if (pantographState != null)
                context.data.putBoolean("Forward", pantographState.facingForward);
        }
        if (context.disabled) {
            if (!prevDisabled)
                ((IPantographList) e.getCarriage()).changePantographState(context.localPos, false);
        } else {
            if (prevDisabled) {
                ((IPantographList) e.getCarriage()).changePantographState(context.localPos, true);
            }
        }

        if (context.world.isClientSide && !context.disabled) {
            Vec3 distance = null;

            float halfPantoReach = 0.5f;
            Vec3 pantographPos = new Vec3(0, -0.75f, 0);
            pantographPos = context.rotation.apply(pantographPos);
            pantographPos = pantographPos.add(context.position);
//            context.world.addParticle(ParticleTypes.SCULK_CHARGE_POP, pantographPos.x, pantographPos.y, pantographPos.z, 0, 0, 0);

            for (Pair<InWorldNodeConnection, WireData> connection : WireRenderer.WIRE_CONNECTIONS) {
                if (connection.getSecond().wireType().getSag() != 0)
                    continue;
                Vec3 start = connection.getFirst().node1().getPosition(context.world);
                Vec3 end = connection.getFirst().node2().getPosition(context.world);
                if (start == null || end == null)
                    continue;
                float halfThickness = connection.getSecond().wireType().getThickness() * 0.5f;
                Vec3 cp = checkCatenary(start, end, pantographPos, context, halfPantoReach);
                if (cp != null) {
                    if (distance == null || distance.y < cp.y)
                        distance = cp.add(0, -halfThickness, 0);
                }
            }
            context.data.putDouble("PrevDistanceY", context.data.getDouble("DistanceY"));
            context.data.putDouble("DistanceY", distance == null ? (Math.min(0.05, context.data.getDouble("DistanceY") + 0.05)) : distance.y);
        } else if (context.world.isClientSide) {
            context.data.putDouble("PrevDistanceY", context.data.getDouble("DistanceY"));
            context.data.putDouble("DistanceY", (Math.min(0.05, context.data.getDouble("DistanceY") + 0.05)));
        }
        context.data.putBoolean("Disabled", context.disabled);
    }

    @Override
    public boolean mustTickWhileDisabled() {
        return true;
    }

    Vec3 checkCatenary(Vec3 start, Vec3 end, Vec3 pantographPos, MovementContext context, float halfPantoReach) {

        double t = 0;

        Vec3 ab = end.subtract(start);
        Vec3 ap = pantographPos.subtract(start);
        double denom = ab.lengthSqr();
        if (denom != 0) {
            t = ap.dot(ab) / denom;

        }

        if (t < -0.01 || t > 1.01)
            return null;
        Vec3 closest = VecHelper.lerp((float) t, start, end);

        Vec3 distance = pantographPos.subtract(closest);
        distance = VecHelper.rotate(distance, -context.contraption.entity.getViewYRot(0), Direction.Axis.Y);
        float xTol = (float) ((distance.y + halfPantoReach) * 0.2f + 0.125f);
        if (Math.abs(distance.z) < 0.5f && Math.abs(distance.x) < xTol && Math.abs(distance.y) < halfPantoReach)
            return distance;

        return null;
    }

    @Override
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {
        double distanceY = Mth.lerp(AnimationTickHolder.getPartialTicks(), context.data.getDouble("PrevDistanceY"), context.data.getDouble("DistanceY"));

        PoseStack ms = matrices.getViewProjection();
        BlockState state = context.state;
        int light = LevelRenderer.getLightColor(renderWorld, context.localPos);

        float yRot = state.getValue(FACING).toYRot();
        if (state.getValue(FACING).getAxis() == Direction.Axis.X)
            yRot += 180;

        CachedBuffers.partial(CEEPartialModels.RAIL_CONTACT_SHOE_CONTACT, state)
                .transform(matrices.getModel())
                .center().rotateYDegrees(yRot).uncenter()
                .translate(0.5f, -distanceY, 0.5f)
                .light(light)
                .useLevelLight(context.world, matrices.getWorld())
                .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

        CachedBuffers.partial(CEEPartialModels.RAIL_CONTACT_SHOE_HINGES, state)
                .transform(matrices.getModel())
                .center().rotateYDegrees(yRot).uncenter()
                .translate(0.5f, -distanceY, 0.5f)
                .rotateX((float) -Mth.atan2(-distanceY - 0.25f, 0.5f) + Mth.PI)
                .scaleZ(1.5f)
                .light(light)
                .useLevelLight(context.world, matrices.getWorld())
                .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
    }

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }
}
