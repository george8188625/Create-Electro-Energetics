package com.george_vi.electroenergetics.content.catenary;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.wire.WireRenderer;
import com.george_vi.electroenergetics.mixin_interfaces.IPantographList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class PantographMovementBehaviour implements MovementBehaviour {
    @Override
    public void tick(MovementContext context) {
        float targetExtensionState = context.blockEntityData.getFloat("ExtensionState");
        float currentExtensionState = context.data.getFloat("CurrentExtensionState");
        float prevExtensionState = currentExtensionState;
        currentExtensionState = Mth.lerp(0.1f, currentExtensionState, targetExtensionState);
        if (Math.abs(currentExtensionState - targetExtensionState) < 0.01)
             currentExtensionState = targetExtensionState;

        if (context.contraption.entity instanceof CarriageContraptionEntity e) {

            boolean prevDisabled = context.data.getBoolean("Disabled");
            if (context.disabled) {
                if (!prevDisabled) {
                    List<Pair<BlockPos, Boolean>> list = ((IPantographList) e.getCarriage()).getPantographList();
                    boolean forward = list.stream().filter(p -> p.getFirst().equals(context.localPos)).findFirst().orElse(Pair.of(BlockPos.ZERO, false)).getSecond();
                    context.data.putBoolean("Forward", forward);
                    list.removeIf(p -> p.getFirst().equals(context.localPos));
                    ((IPantographList) e.getCarriage()).setPantographList(list);
                    targetExtensionState = 0;
                }
            } else {
                if (prevDisabled) {
                    List<Pair<BlockPos, Boolean>> list = ((IPantographList) e.getCarriage()).getPantographList();
                    if (!list.stream().anyMatch(p -> p.getFirst().equals(context.localPos))) {
                        list.add(Pair.of(context.localPos, context.data.getBoolean("Forward")));
                        ((IPantographList) e.getCarriage()).setPantographList(list);
                    }
                    targetExtensionState = 0.75f;
                }
            }
        }

        // Look for the connection point, and try to adjust the pantographs extension state to fit that point

        if (context.world.isClientSide) {
            Vec3 pantographPos = new Vec3(0, 1, 0);
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

                if (!(Math.abs(distance.z()) > 1.5) && !(Math.abs(distance.x()) > 0.5) && !(Math.abs(distance.y()) > 1)) {
                    connectionPoint = closest;
                    break;
                }
            }

            if (connectionPoint != null) {
                float lo = 0;
                float hi = 1.3f;
                for (int i = 0; i < 10; i++) {
                    float m1 = lo + (hi - lo) / 3;
                    float m2 = hi - (hi - lo) / 3;
                    if (getConnectorPos(m1, context).distanceTo(connectionPoint) < getConnectorPos(m2, context).distanceTo(connectionPoint))
                        hi = m2;
                    else
                        lo = m1;
                }
                targetExtensionState = (lo + hi) / 2;
            }
//            Vec3 connectorPlatePos = getConnectorPos(currentExtensionState, context);

        }

        context.data.putBoolean("Disabled", context.disabled);

        context.data.putFloat("CurrentExtensionState", currentExtensionState);
        context.blockEntityData.putFloat("ExtensionState", targetExtensionState);
        context.data.putFloat("PrevExtensionState", prevExtensionState);
    }

    Vec3 getConnectorPos(float extensionState, MovementContext context) {
        double armHingePosY = Math.cos((-75+extensionState*30)*Math.PI/180)*1.5;
        double armHingePosX = Math.sin((-75+extensionState*30)*Math.PI/180)*1.5;

        Vec3 connectorPlatePos = new Vec3(0, armHingePosY, armHingePosX).add(0, Math.cos((89-extensionState*30)*Math.PI/180)*1.5, Math.sin((89-extensionState*30)*Math.PI/180)*1.5);
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

        CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_LOWER_ARM, state)
                .transform(matrices.getModel())
                .center().rotateYDegrees(yRot).uncenter()
                .translate(0, 0.375, 0.8125)
                .rotateXDegrees(-75+extensionState*30)
                .light(light)
                .useLevelLight(context.world, matrices.getWorld())
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));

        double armHingePosY = Math.cos((-75+extensionState*30)*Math.PI/180)*1.5;
        double armHingePosX = Math.sin((-75+extensionState*30)*Math.PI/180)*1.5;

        CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARM, state)
                .transform(matrices.getModel())
                .center().rotateYDegrees(yRot).uncenter()
                .translate(0, 0.375, 0.8125)
                .translate(0, armHingePosY, armHingePosX)
                .rotateXDegrees(-1-extensionState*30)
                .light(light)
                .useLevelLight(context.world, matrices.getWorld())
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));

        CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_CONNECTING_SURFACE, state)
                .transform(matrices.getModel())
                .center().rotateYDegrees(yRot).uncenter()
                .translate(0, 0.375, 0.8125)
                .translate(0, armHingePosY, armHingePosX)
                .translate(0, Math.cos((89-extensionState*30)*Math.PI/180)*1.5, Math.sin((89-extensionState*30)*Math.PI/180)*1.5)
                .light(light)
                .useLevelLight(context.world, matrices.getWorld())
                .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

        CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_SPRINGS, state)
                .transform(matrices.getModel())
                .center().rotateYDegrees(yRot).uncenter()
                .translate(0, 0.375, 0.25)
                .rotateXDegrees(-22+extensionState*-20)
                .scale(1, 1, 1+extensionState/2)
                .light(light)
                .useLevelLight(context.world, matrices.getWorld())
                .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

        CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_CONNECTING_ROD, state)
                .transform(matrices.getModel())
                .center().rotateYDegrees(yRot).uncenter()
                .translate(0, 0.375, 0.1875)
                .rotateXDegrees(-77+extensionState*43)
                .light(light)
                .useLevelLight(context.world, matrices.getWorld())
                .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
    }

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }
}
