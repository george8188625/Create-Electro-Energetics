package com.george_vi.electroenergetics.content.railway_electrification.pantograph;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.mixin_interfaces.IPantographList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class PantographMovementBehaviour implements MovementBehaviour {
    @Override
    public void tick(MovementContext context) {
        float targetExtensionState = context.blockEntityData.getFloat("ExtensionState");
        float currentExtensionState = context.data.getFloat("CurrentExtensionState");
        float prevExtensionState = currentExtensionState;
        if (targetExtensionState == 0)
            currentExtensionState = Mth.lerp(0.1f, currentExtensionState, targetExtensionState);
        else
            currentExtensionState = Mth.lerp(1f, currentExtensionState, targetExtensionState);
        if (Math.abs(currentExtensionState - targetExtensionState) < 0.01)
             currentExtensionState = targetExtensionState;

        boolean isDouble = context.state.getValue(PantographBlock.DOUBLE);

        if (context.contraption.entity instanceof CarriageContraptionEntity e) {
            boolean prevDisabled = context.data.getBoolean("Disabled");
            if (!context.data.contains("Forward")) {
                TrainPantographEntry pantographState = ((IPantographList) e.getCarriage()).getPantographState(context.localPos);
                if (pantographState != null)
                    context.data.putBoolean("Forward", pantographState.facingForward());
            }
            if (context.disabled) {
                if (!prevDisabled) {
                    ((IPantographList) e.getCarriage()).changePantographState(context.localPos, false);
                    targetExtensionState = context.state.getValue(PantographBlock.DOUBLE) ? 0.3f : 0f;
                }
            } else {
                if (prevDisabled) {
                    ((IPantographList) e.getCarriage()).changePantographState(context.localPos, true);
                    targetExtensionState = 0.75f;
                }
            }
        }

        // Look for the connection point, and try to adjust the pantographs extension state to fit that point

        if (context.world.isClientSide && !context.disabled) {
            Vec3 pantographPos = new Vec3(context.state.getValue(PantographBlock.FACING).getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 0.5 : -0.5, (isDouble ? 1.5f : 1.75f), 0);
            pantographPos = context.rotation.apply(pantographPos);
            pantographPos = pantographPos.add(context.position);

            List<Couple<BlockPos>> allCatenaryConnections = List.copyOf(WireRenderer.CATENARY);
            Vec3 connectionPoint = null;
            for (Couple<BlockPos> connection : allCatenaryConnections) {
                Vec3 start = connection.getFirst().getBottomCenter();
                Vec3 end = connection.getSecond().getBottomCenter();

                double t = 0;

                Vec3 ab = end.subtract(start);
                Vec3 ap = pantographPos.subtract(start);
                double denom = ab.lengthSqr();
                if (denom != 0) {
                    t = ap.dot(ab) / denom;
                    t = Math.max(0, Math.min(1, t));
                }

                Vec3 closest = VecHelper.lerp((float) t, start, end);

                Vec3 distance = pantographPos.subtract(closest);
                context.rotation.apply(distance).multiply(0, 0, 0);

                if (Math.abs(distance.z()) < 1.5 && Math.abs(distance.x()) < 0.5 && Math.abs(distance.y()) < (isDouble ? 1.5f : 1.75f)) {
                    connectionPoint = closest;
                    break;
                }
            }

            if (connectionPoint != null) {
                context.world.addParticle(ParticleTypes.ELECTRIC_SPARK, connectionPoint.x, connectionPoint.y, connectionPoint.z, 0, 0, 0);
                float lo = 0;
                float hi = 2.2f;
                for (int i = 0; i < 20; i++) {
                    float m1 = lo + (hi - lo) / 3;
                    float m2 = hi - (hi - lo) / 3;
                    if (Math.abs(getConnectorPos(m1, context).y - connectionPoint.y) < Math.abs(getConnectorPos(m2, context).y - connectionPoint.y))
                        hi = m2;
                    else
                        lo = m1;
                }
                targetExtensionState = (lo + hi) / 2;
            }

        }

        context.data.putBoolean("Disabled", context.disabled);

        context.data.putFloat("CurrentExtensionState", currentExtensionState);
        context.blockEntityData.putFloat("ExtensionState", targetExtensionState);
        context.data.putFloat("PrevExtensionState", prevExtensionState);
    }

    Vec3 getConnectorPos(float extensionState, MovementContext context) {
        if (context.state.getValue(PantographBlock.DOUBLE)) {
            float lowerArmRadians = (-90 + extensionState * 27) * Mth.DEG_TO_RAD;
            double armHingePosY = Mth.cos(lowerArmRadians) * 1.5;
            double armHingePosX = Mth.sin(lowerArmRadians) * 1.5;

            float upperArmRadians = (89 - extensionState * 30) * Mth.DEG_TO_RAD;
            Vec3 connectorPlatePos = new Vec3(0, armHingePosY, armHingePosX)
                    .add(0, Mth.cos(upperArmRadians) * 1.5 + 0.3f, Mth.sin(upperArmRadians) * 1.9);
            connectorPlatePos = context.rotation.apply(connectorPlatePos);
            connectorPlatePos = connectorPlatePos.add(context.position);
            return connectorPlatePos;
        }
        float lowerArmRadians = (-75 + extensionState * 30) * Mth.DEG_TO_RAD;

        double armHingePosY = Mth.cos(lowerArmRadians) * 1.875;
        double armHingePosX = Mth.sin(lowerArmRadians) * 1.875;

        float upperArmRadians = (89 - extensionState * 50) * Mth.DEG_TO_RAD;
        Vec3 connectorPlatePos = new Vec3(0, armHingePosY, armHingePosX).add(0, Mth.cos(upperArmRadians) * 1.9, Mth.sin(upperArmRadians) * 1.9);
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

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARMS_DOUBLE, state)
                    .transform(matrices.getModel())
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.5)
                    .translate(0, armHingePosY, armHingePosX)
                    .rotateXDegrees(83 - extensionState * rotationFactor * 0.8f)
                    .color(color)
                    .light(light)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_CONNECTING_SURFACE_DOUBLE, state)
                    .transform(matrices.getModel())
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 1)
                    .translate(0, Mth.cos((-75 + extensionState * 30) * Mth.DEG_TO_RAD) * 1.5 + Mth.cos((89 - extensionState * 30) * Mth.DEG_TO_RAD) * 1.5, 0)
                    .light(light)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_SPRINGS_DOUBLE, state)
                    .transform(matrices.getModel())
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.6, 0.6)
                    .translate(0, armHingePosY * 0.2f, armHingePosX * 0.3f)
                    .scaleZ((float) (1.0 - (0.7f + armHingePosX * 0.3f)) * 1.4f)
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

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }
}
