package com.george_vi.electroenergetics.content.railway_electrification.pantograph;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class PantographMovementBehaviour implements MovementBehaviour {

    @Override
    public void tick(MovementContext context) {
        float targetExtensionState = context.blockEntityData.getFloat("ExtensionState");
        float currentExtensionState = context.data.getFloat("CurrentExtensionState");
        float prevExtensionState = currentExtensionState;
        currentExtensionState = currentExtensionState < targetExtensionState ? Math.min(targetExtensionState, currentExtensionState + 0.05f) : Math.max(targetExtensionState, currentExtensionState - 0.05f);

        boolean isDouble = context.state.getValue(PantographBlock.DOUBLE);

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
            targetExtensionState = 0f;
        } else {
            if (prevDisabled) {
                ((IPantographList) e.getCarriage()).changePantographState(context.localPos, true);
                targetExtensionState = 0.75f;
            }
        }

        if (context.world.isClientSide && currentExtensionState == targetExtensionState && prevExtensionState != currentExtensionState)
            context.world.playLocalSound(context.position.x, context.position.y, context.position.z, SoundEvents.CHAIN_HIT, SoundSource.NEUTRAL, 0.1f, 1f, false);



        // Look for the connection point, and try to adjust the pantographs extension state to fit that point

        if (context.world.isClientSide && !context.disabled) {
            double pantographX = context.state.getValue(PantographBlock.FACING).getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 0.5 : -0.5;
            if (!isDouble)
                pantographX /= 2;
            float halfPantoReach = 1.625f;
            Direction.Axis axis = context.state.getValue(FACING).getAxis();
            Vec3 pantographPos = new Vec3(axis == Direction.Axis.X ? pantographX : 0, halfPantoReach, axis == Direction.Axis.Z ? pantographX : 0);
            pantographPos = context.rotation.apply(pantographPos);
            pantographPos = pantographPos.add(context.position);

            Vec3 connectionPoint = null;
            if (CEEConfigs.client().debugPantographRange.get())
                context.world.addParticle(ParticleTypes.SCRAPE, pantographPos.x, pantographPos.y, pantographPos.z, 0, 0, 0);
            for (CatenaryConnection connection : WireRenderer.CATENARY) {
                Vec3 start = connection.pos1().getBottomCenter();
                Vec3 end = connection.pos2().getBottomCenter();
                Vec3 cp = checkCatenary(start, end, pantographPos, context, halfPantoReach);
                if (cp != null) {
                    if (connectionPoint == null || connectionPoint.y > cp.y)
                        connectionPoint = cp;
                }
            }

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
                    if (connectionPoint == null || connectionPoint.y > cp.y)
                        connectionPoint = cp.add(0, -halfThickness + 0.015f, 0);
                }
            }

            if (connectionPoint != null) {
                float lo = 0;
                float hi = 2.7f;
                for (int i = 0; i < 20; i++) {
                    float m1 = lo + (hi - lo) / 3;
                    float m2 = hi - (hi - lo) / 3;
                    if (Math.abs(getConnectorPos(m1, context).y - connectionPoint.y) < Math.abs(getConnectorPos(m2, context).y - connectionPoint.y))
                        hi = m2;
                    else
                        lo = m1;
                }
//                context.world.addParticle(ParticleTypes.SCULK_CHARGE_POP, connectionPoint.x, connectionPoint.y, connectionPoint.z, 0, 0, 0);
                float newExtensionState = (lo + hi) / 2;
                if (targetExtensionState == currentExtensionState && targetExtensionState != 0)
                    currentExtensionState = newExtensionState;

                targetExtensionState = newExtensionState;
            } else {
                targetExtensionState = 0f;
            }

            if (CEEConfigs.client().debugPantographRange.get() && (AnimationTickHolder.getTicks() % 3) == 0) {
                Vec3 cp = getConnectorPos(currentExtensionState, context);
                context.world.addParticle(ParticleTypes.ELECTRIC_SPARK, cp.x, cp.y, cp.z, 0, 0, 0);

                Vec3 pp = new Vec3(axis == Direction.Axis.X ? pantographX : 0, halfPantoReach, axis == Direction.Axis.Z ? pantographX : 0);
                for (float y = -halfPantoReach; y <= halfPantoReach; y += 2 / 16f) {
                    float v = (y + halfPantoReach) * 0.1f + 0.125f;
                    v = v - (v % (1 / 16f));
                    for (float x = -v; x <= v; x += 2 / 16f) {
                        for (float z = -1.5f; z <= 1.5f; z += 2 / 16f) {
                            boolean edgeX = x == -v || x == v;
                            boolean edgeY = y == -halfPantoReach || y == halfPantoReach;
                            boolean edgeZ = z == -1.5f || z == 1.5f;
                            if ((edgeX && edgeY && !edgeZ) || (edgeX && !edgeY && edgeZ) || (!edgeX && edgeY && edgeZ)) {
                                Vec3 pos = context.rotation.apply(pp.add(axis == Direction.Axis.X ? x : z, y - 0.25f, axis == Direction.Axis.X ? z : x)).add(context.position);
                                context.world.addParticle(ParticleTypes.DOLPHIN, pos.x, pos.y, pos.z, 0, 0, 0);
                            }

                        }
                    }
                }
            }
        }

        context.data.putBoolean("Disabled", context.disabled);

        context.data.putFloat("CurrentExtensionState", currentExtensionState);
        context.blockEntityData.putFloat("ExtensionState", targetExtensionState);
        context.data.putFloat("PrevExtensionState", prevExtensionState);
    }

    Vec3 getConnectorPos(float extensionState, MovementContext context) {
        Direction.Axis axis = context.state.getValue(PantographBlock.FACING).getAxis();
        if (context.state.getValue(PantographBlock.DOUBLE)) {
            float lowerArmRadians = (-90 + extensionState * 27) * Mth.DEG_TO_RAD;
            double armHingePosY = Mth.cos(lowerArmRadians) * 1.5;
            double armHingePosX = Mth.sin(lowerArmRadians) * 1.5;

            float b = (float) (-0.4f - Math.abs(armHingePosX));
            float a = Mth.sqrt(-b * b + 2f * 2f);

            double pantographX = context.state.getValue(PantographBlock.FACING).getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 0.5 : -0.5;
            Vec3 connectorPlatePos = new Vec3(axis == Direction.Axis.X ? pantographX : 0, 0.1875 + a + armHingePosY, axis == Direction.Axis.Z ? pantographX : 0);
            connectorPlatePos = context.rotation.apply(connectorPlatePos);
            connectorPlatePos = connectorPlatePos.add(context.position);
            return connectorPlatePos;
        }
        float lowerArmRadians = (-75 + extensionState * 30) * Mth.DEG_TO_RAD;

        double armHingePosY = Mth.cos(lowerArmRadians) * 1.875;
        double armHingePosX = Mth.sin(lowerArmRadians) * 1.875;

        float upperArmRadians = (89 - extensionState * 50) * Mth.DEG_TO_RAD;
        double pantographX = context.state.getValue(PantographBlock.FACING).getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 0.25 : -0.25;
        Vec3 connectorPlatePos = new Vec3((axis == Direction.Axis.X ? pantographX : 0), armHingePosY, armHingePosX + (axis == Direction.Axis.Z ? pantographX : 0)).add(0, Mth.cos(upperArmRadians) * 1.9, Mth.sin(upperArmRadians) * 1.9);
        connectorPlatePos = context.rotation.apply(connectorPlatePos);
        connectorPlatePos = connectorPlatePos.add(context.position);
        return connectorPlatePos;
    }

    @Override
    public boolean mustTickWhileDisabled() {
        return true;
    }

    @Override
    public void onDisabledByControls(MovementContext context) {
        MovementBehaviour.super.onDisabledByControls(context);
    }

    @Override
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {
        PoseStack ms = matrices.getViewProjection();
        BlockState state = context.state;
        float extensionState = Mth.lerp(AnimationTickHolder.getPartialTicks(), context.data.getFloat("PrevExtensionState"), context.data.getFloat("CurrentExtensionState"));

        float yRot = state.getValue(FACING).toYRot();
        if (state.getValue(FACING).getAxis() == Direction.Axis.Z)
            yRot += 180;

        int light = LevelRenderer.getLightColor(renderWorld, context.localPos);
        int color = DyeColor.byName(context.blockEntityData.getString("Color"), DyeColor.WHITE).getTextureDiffuseColor();
        if (state.getValue(PantographBlock.DOUBLE)) {
            float rotationFactor = 27;

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_BASE_DOUBLE, state)
                    .transform(matrices.getModel())
                    .center().rotateYDegrees(yRot).uncenter()
                    .color(color)
                    .light(light)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_LOWER_ARMS_DOUBLE, state)
                    .transform(matrices.getModel())
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.5)
                    .rotateXDegrees(-90 + extensionState * rotationFactor)
                    .color(color)
                    .light(light)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            float lowerArmRadians = (-90 + extensionState * rotationFactor) * Mth.DEG_TO_RAD;
            double armHingePosY = Mth.cos(lowerArmRadians) * 1.5;
            double armHingePosX = Mth.sin(lowerArmRadians) * 1.5;

            float b = (float) (-0.4f - Math.abs(armHingePosX));
            float a = Mth.sqrt(-b * b + 2f * 2f);


            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARMS_DOUBLE, state)
                    .transform(matrices.getModel())
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, armHingePosY + 0.375, armHingePosX + 0.5)
                    .rotateX((float) Mth.atan2(a, b) - Mth.HALF_PI)
                    .color(color)
                    .light(light)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_CONNECTING_SURFACE_DOUBLE, state)
                    .transform(matrices.getModel())
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.5625 + a + armHingePosY, 1)
                    .light(light)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            float springHingePosY = Mth.cos(lowerArmRadians + 0.5f) * 0.425f;
            float springHingePosX = Mth.sin(lowerArmRadians + 0.5f) * 0.425f;

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_SPRINGS_DOUBLE, state)
                    .transform(matrices.getModel())
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, springHingePosY + 0.375, springHingePosX + 0.5f)
                    .scaleZ((-springHingePosX + 0.5f) * (16/13f))
                    .rotateXDegrees(0)
                    .light(light)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

        } else {
            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_BASE, state)
                    .transform(matrices.getModel())
                    .center().rotateYDegrees(yRot).uncenter()
                    .color(color)
                    .light(light)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_LOWER_ARM, state)
                    .transform(matrices.getModel())
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.8125)
                    .rotateXDegrees(-75 + extensionState * 30)
                    .color(color)
                    .light(light)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));
            float lowerArmRadians = (-75 + extensionState * 30) * Mth.DEG_TO_RAD;

            double armHingePosY = Mth.cos(lowerArmRadians) * 1.875;
            double armHingePosX = Mth.sin(lowerArmRadians) * 1.875;

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARM, state)
                    .transform(matrices.getModel())
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.8125)
                    .translate(0, armHingePosY, armHingePosX)
                    .rotateXDegrees(-1 - extensionState * 50)
                    .color(color)
                    .light(light)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARM_ARM, state)
                    .transform(matrices.getModel())
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(12/16f, 0.375, 0.8125)
                    .translate(0, armHingePosY, armHingePosX)
                    .rotateXDegrees(-1 - extensionState * 50)
                    .rotateYDegrees(12.5f)
                    .color(color)
                    .light(light)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARM_ARM, state)
                    .transform(matrices.getModel())
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(4/16f, 0.375, 0.8125)
                    .translate(0, armHingePosY, armHingePosX)
                    .rotateXDegrees(-1 - extensionState * 50)
                    .rotateYDegrees(-12.5f)
                    .color(color)
                    .light(light)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            float upperArmRadians = (89 - extensionState * 50) * Mth.DEG_TO_RAD;
            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_CONNECTING_SURFACE, state)
                    .transform(matrices.getModel())
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.8125)
                    .translate(0, armHingePosY, armHingePosX)
                    .translate(0, Mth.cos(upperArmRadians) * 1.9, Mth.sin(upperArmRadians) * 1.9)
                    .light(light)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_SPRINGS, state)
                    .transform(matrices.getModel())
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.25)
                    .rotateXDegrees(-22 + extensionState * -20)
                    .scale(1, 1, 1 + extensionState / 2)
                    .light(light)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_CONNECTING_ROD, state)
                    .transform(matrices.getModel())
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.1875)
                    .rotateXDegrees(-77 + extensionState * 43)
                    .color(color)
                    .light(light)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
        }
    }

    Vec3 checkCatenary(Vec3 start, Vec3 end, Vec3 pantographPos, MovementContext context, float halfPantoReach) {

        double t = 0;

        Vec3 ab = end.subtract(start);
        Vec3 ap = pantographPos.subtract(start);
        double denom = ab.lengthSqr();
        if (denom != 0)
            t = ap.dot(ab) / denom;
        if (t < -0.01 || t > 1.01)
            return null;

        Vec3 closest = VecHelper.lerp((float) t, start, end);
//        Vec3 closest = Minecraft.getInstance().player.position();
//        context.world.addParticle(ParticleTypes.SCULK_CHARGE_POP, closest.x, closest.y, closest.z, 0, 0, 0);
//        context.world.addParticle(ParticleTypes.SCULK_CHARGE_POP, pantographPos.x, pantographPos.y, pantographPos.z, 0, 0, 0);

        Vec3 distance = pantographPos.subtract(closest);
        distance = VecHelper.rotate(distance, -context.contraption.entity.getViewYRot(0), Direction.Axis.Y);
        float xTol = (float) ((distance.y + halfPantoReach) * 0.2f + 0.125f);
        if (Math.abs(distance.z) < 1.5 && Math.abs(distance.x) < xTol && Math.abs(distance.y) < halfPantoReach)
            return closest;

        return null;
    }

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }
}
