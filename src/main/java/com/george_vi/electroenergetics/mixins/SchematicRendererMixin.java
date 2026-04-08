package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.mixin_interfaces.ISchematicInfrastructureList;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.schematics.client.SchematicRenderer;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This makes saved infrastructure data (wires) render in schematics.
 */
@Mixin(SchematicRenderer.class)
public class SchematicRendererMixin {

    @Shadow
    @Final
    protected SchematicLevel schematic;

    @Inject(method = "render", at=@At("TAIL"), remap = false)
    public void createElectroEnergetics$render(PoseStack ms, SuperRenderTypeBuffer buffers, CallbackInfo ci) {
        if (!createElectroEnergetics$shouldRenderInSchematic() ||
                !(schematic instanceof ISchematicInfrastructureList infrastructure))
            return;

        Map<InWorldNode, List<Vec3>> outerInsulatorJumpers = new HashMap<>();
        for (Map.Entry<InWorldNodeConnection, WireData> wire : infrastructure.getWireConnections().entrySet()) {
            InWorldNodeConnection connection = wire.getKey();
            WireData wireData = wire.getValue();

            BlockState state1 = schematic.getBlockState(connection.node1().sourcePos());
            BlockState state2 = schematic.getBlockState(connection.node2().sourcePos());

            Vec3 pos1 = connection.node1().getPosition(schematic);
            Vec3 pos2 = connection.node2().getPosition(schematic);

            if (pos1 == null || pos2 == null) {
                pos1 = connection.node1().sourcePos().getCenter();
                pos2 = connection.node2().sourcePos().getCenter();
            }

            boolean isBlock1Outer = state1.getBlock() instanceof ElectricalDeviceBlock<?> db &&
                    db.isOuterInsulator(schematic, connection.node1().sourcePos(), state1, connection.node1().id());
            boolean isBlock2Outer = state2.getBlock() instanceof ElectricalDeviceBlock<?> db &&
                    db.isOuterInsulator(schematic, connection.node2().sourcePos(), state2, connection.node2().id());

            for (Pair<Float, WireAttachment> attachment : wireData.attachments()) {
                Vec3 offset = QuadraticWireHelper.posAt(pos1, pos2, attachment.getFirst(), wireData.wireType().getSag());
                float elevation = QuadraticWireHelper.pointElevationInDegrees(pos1, pos2, attachment.getFirst(), wireData.wireType().getSag());

                ms.pushPose();
                PoseTransformStack msr = TransformStack.of(ms);
                msr.translate(offset);
                double angleY = Math.toDegrees(Math.atan2(pos2.x - pos1.x, pos2.z - pos1.z)) + 90;
                msr.rotateYDegrees((float) angleY);
                msr.rotateXDegrees(180);
                BlockPos pos = BlockPos.containing(offset);
                int light = createElectroEnergetics$getLightColor(pos);
                attachment.getSecond().type.render(ms, buffers, attachment.getSecond(), offset, light, elevation);
                ms.popPose();
            }

            List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wireData.wireType().getSag());

            WireRenderer.renderWire(points, pos1, pos2, ms, buffers, wireData.wireType(), schematic);

            if (points.size() >= 10) {
                if (isBlock1Outer) {
                    Vec3 nextPoint = points.get(5);
                    CachedBuffers.partial(CEEPartialModels.INSULATOR, Blocks.ANDESITE.defaultBlockState())
                            .translate(pos1)
                            .rotateY((float) Math.atan2(nextPoint.x() - pos1.x(), nextPoint.z() - pos1.z()))
                            .rotateX(-(float) Math.atan2(nextPoint.y - pos1.y, Math.hypot(nextPoint.x - pos1.x, nextPoint.z - pos1.z)))
                            .light(createElectroEnergetics$getLightColor(BlockPos.containing(pos1.add(nextPoint).multiply(0.5, 0.5, 0.5))))
                            .renderInto(ms, buffers.getBuffer(RenderType.solid()));

                    if (!outerInsulatorJumpers.containsKey(connection.node1()))
                        outerInsulatorJumpers.put(connection.node1(), new ArrayList<>());
                    outerInsulatorJumpers.get(connection.node1()).add(nextPoint);
                }

                if (isBlock2Outer) {
                    Vec3 nextPoint = points.get(points.size() - 6);
                    CachedBuffers.partial(CEEPartialModels.INSULATOR, Blocks.ANDESITE.defaultBlockState())
                            .translate(pos2)
                            .rotateY((float) Math.atan2(nextPoint.x() - pos2.x(), nextPoint.z() - pos2.z()))
                            .rotateX(-(float) Math.atan2(nextPoint.y - pos2.y, Math.hypot(nextPoint.x - pos2.x, nextPoint.z - pos2.z)))
                            .light(createElectroEnergetics$getLightColor(BlockPos.containing(pos2.add(nextPoint).multiply(0.5, 0.5, 0.5))))
                            .renderInto(ms, buffers.getBuffer(RenderType.solid()));

                    if (!outerInsulatorJumpers.containsKey(connection.node2()))
                        outerInsulatorJumpers.put(connection.node2(), new ArrayList<>());
                    outerInsulatorJumpers.get(connection.node2()).add(nextPoint);
                }
            }
        }
    }

    @Unique
    private int createElectroEnergetics$getLightColor(BlockPos pos) {
        return LevelRenderer.getLightColor(schematic, pos);
    }

    @Unique
    private static boolean createElectroEnergetics$shouldRenderInSchematic() {
        return true;
    }
}
