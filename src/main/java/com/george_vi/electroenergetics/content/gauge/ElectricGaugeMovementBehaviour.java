package com.george_vi.electroenergetics.content.gauge;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.ElectricTrainData;
import com.george_vi.electroenergetics.content.railway_electrification.gauges.ClientTrainGaugeData;
import com.george_vi.electroenergetics.mixin_interfaces.ICEETrainExtension;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.kinetics.gauge.GaugeBlock;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Movement behaviour for electric gauges (voltmeters and ammeters) on train contraptions.
 * Reads voltage and current data from the train and displays it on the gauge dial.
 */
public class ElectricGaugeMovementBehaviour implements MovementBehaviour {
    private static final float DIAL_SMOOTHING_FACTOR = 0.25f;

    private final boolean voltmeter;

    public ElectricGaugeMovementBehaviour(boolean voltmeter) {
        this.voltmeter = voltmeter;
    }

    @Override
    public void tick(MovementContext context) {
        double voltage = 0;
        double current = 0;

        if (context.contraption.entity instanceof CarriageContraptionEntity carriageEntity) {
            // Get train ID and look up values in cache
            var carriage = carriageEntity.getCarriage();
            if (carriage == null || carriage.train == null)
                return;

            java.util.UUID trainId = carriage.train.id;

            if (context.world.isClientSide()) {
                // On client, read from synced cache
                ClientTrainGaugeData.TrainGaugeValues values =
                    ClientTrainGaugeData.get(trainId);
                voltage = values.voltage();
                current = values.current();
            } else {
                // On server, read directly from train data
                if (carriage.train instanceof ICEETrainExtension trainExtension) {
                    ElectricTrainData trainData = trainExtension.getElectricTrainData();
                    voltage = trainData.lastVoltage;
                    current = trainData.displayCurrent;
                }
            }
        }

        // Calculate dial target based on gauge type
        // (note that this doesn't include accumulators)
        float dialTarget;
        if (voltmeter) {
            // Scale from train minimum voltage to maximum train voltage
            double minVoltage = CEEConfigs.server().voltageValues.trainMinVoltage.get();
            double maxVoltage = CEEConfigs.server().voltageValues.trainMaxVoltage.get();
            dialTarget = (float) Mth.clamp((voltage - minVoltage) / (maxVoltage - minVoltage), 0, 1);
        } else {
            // Scale from 0 to maximum theoretical current
            // Max current = Max Power / Min voltage
            double minVoltage = CEEConfigs.server().voltageValues.trainMinVoltage.get();
            double maxPower = Math.max(
                CEEConfigs.server().resistanceValues.electricTrainAccelerationPowerConsumption.get(),
                CEEConfigs.server().resistanceValues.electricTrainCruisePowerConsumption.get()
            );
            double maxCurrent = maxPower / minVoltage;
            dialTarget = (float) Mth.clamp(current / maxCurrent, 0, 1);
        }

        boolean firstTick = !context.data.contains("Initialized");

        // Get previous frame's state for interpolation
        float prevDialState = firstTick ? dialTarget : context.data.getFloat("DialState");
        float currentDialState;

        if (firstTick) {
            // First tick: start at target
            currentDialState = dialTarget;
            context.data.putBoolean("Initialized", true);
        } else {
            // Normal operation: smooth interpolation toward target
            currentDialState = prevDialState + (dialTarget - prevDialState) * DIAL_SMOOTHING_FACTOR;
        }

        // Store updated state for next tick
        context.data.putFloat("PrevDialState", prevDialState);
        context.data.putFloat("DialState", currentDialState);
        context.data.putFloat("DialTarget", dialTarget);
    }

    @Override
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
                                     ContraptionMatrices matrices, MultiBufferSource buffer) {
        PoseStack ms = matrices.getViewProjection();
        BlockState state = context.state;

        // Interpolate dial state for smooth animation
        float partialTicks = AnimationTickHolder.getPartialTicks();
        float progress = Mth.lerp(partialTicks,
                context.data.getFloat("PrevDialState"),
                context.data.getFloat("DialState"));

        // Get gauge properties
        Direction facing = state.getValue(ElectricGaugeBlock.FACING);
        boolean axisAlongFirst = state.getValue(GaugeBlock.AXIS_ALONG_FIRST_COORDINATE);

        // Determine which model to use
        PartialModel headModel = voltmeter
                ? AllPartialModels.GAUGE_HEAD_SPEED
                : AllPartialModels.GAUGE_HEAD_STRESS;

        int light = LevelRenderer.getLightColor(renderWorld, context.localPos);

        // Render on the correct face
        for (Direction direction : Iterate.directions) {
            if (!shouldRenderHeadOnFace(state, facing, axisAlongFirst, direction))
                continue;

            float dialPivot = 5.75f / 16f;
            CachedBuffers.partial(AllPartialModels.GAUGE_DIAL, state)
                    .transform(matrices.getModel())
                    .rotateCentered((float) ((-direction.toYRot() - 90) / 180 * Math.PI), Direction.UP)
                    .translate(0, dialPivot, dialPivot)
                    .rotate((float) (Math.PI / 2 * -progress), Direction.EAST)
                    .translate(0, -dialPivot, -dialPivot)
                    .light(light)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            CachedBuffers.partial(headModel, state)
                    .transform(matrices.getModel())
                    .rotateCentered((float) ((-direction.toYRot() - 90) / 180 * Math.PI), Direction.UP)
                    .light(light)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        }
    }

    /**
     * Determines if the gauge head should render on a given face.
     * Replicates the logic from {@link ElectricGaugeBlock#shouldRenderHeadOnFace}
     * for use in the movement behaviour.
     *
     * @param state The block state of the gauge
     * @param facing The direction the gauge is facing
     * @param axisAlongFirst Whether the gauge axis is along the first coordinate
     * @param face The face being checked for rendering
     * @return true if the gauge head should render on this face
     */
    private static boolean shouldRenderHeadOnFace(BlockState state, Direction facing, boolean axisAlongFirst, Direction face) {
        if (face.getAxis().isVertical())
            return false;
        if (face == facing.getOpposite())
            return false;

        Direction.Axis gaugeAxis = getAxis(state, facing, axisAlongFirst);
        if (face.getAxis() == gaugeAxis)
            return false;
        if (gaugeAxis == Direction.Axis.Y && face != facing)
            return false;

        return true;
    }

    /**
     * Gets the axis of the gauge based on its state.
     * Replicates the logic from {@link ElectricGaugeBlock#getAxis} for use in the movement behaviour.
     *
     * @param state The block state of the gauge
     * @param facing The direction the gauge is facing
     * @param axisAlongFirst Whether the gauge axis is along the first coordinate
     * @return The axis of the gauge
     */
    private static Direction.Axis getAxis(BlockState state, Direction facing, boolean axisAlongFirst) {
        Direction.Axis pistonAxis = facing.getAxis();

        if (pistonAxis == Direction.Axis.X)
            return axisAlongFirst ? Direction.Axis.Y : Direction.Axis.Z;
        if (pistonAxis == Direction.Axis.Y)
            return axisAlongFirst ? Direction.Axis.X : Direction.Axis.Z;
        return axisAlongFirst ? Direction.Axis.X : Direction.Axis.Y;
    }

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }
}
