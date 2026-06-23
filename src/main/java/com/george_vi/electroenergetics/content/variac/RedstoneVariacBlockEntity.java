package com.george_vi.electroenergetics.content.variac;

import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RedstoneVariacBlockEntity extends SmartBlockEntity {

    protected ScrollValueBehaviour ratio;
    protected float progress;
    protected LerpedFloat smoothProgress = LerpedFloat.linear();
    protected float targetProgress;

    public RedstoneVariacBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        ratio = new ScrollValueBehaviour(CEELang.translate("variac.ratio").component(),
                this, new ValueBox()) {
            @Override
            public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
                return new ValueSettingsBoard(label, max, ratios.length / 11, ImmutableList.of(CEELang.translate("transformer.ratio_symbol").component()),
                        new ValueSettingsFormatter(valueSettings -> Component.literal(indexToRatio(valueSettings.value()) + CEELang.string("variac.ratio_scaling_symbol"))));
            }
        };

        ratio.between(0, ratios.length);
        ratio.value = ratios.length / 2 - 1;
        ratio.withFormatter(i -> indexToRatio(i) + CEELang.string("variac.ratio_scaling_symbol"));
        ratio.withCallback(i -> this.updateRatio());
        behaviours.add(ratio);
    }

    public static double indexToRatio(int value) {
        return ratios[Mth.clamp(value, 0, ratios.length - 1)];
    }

    @Override
    public void tick() {
        if (level instanceof ServerLevel sl) {
            float prevProgress = progress;
            progress = Mth.lerp(0.6f, progress, targetProgress);

            if (prevProgress != progress) {
                VariacDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, VariacDevice.class);

                if (device != null)
                    device.progress = progress;
                sendData();
            }
        } else {
            smoothProgress.tickChaser();
        }
    }

    private void updateRatio() {
        if (!(level instanceof ServerLevel sl))
            return;

        VariacDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, VariacDevice.class);

        if (device != null)
            device.ratio = indexToRatio(ratio.value);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        progress = tag.getFloat("Progress");
        targetProgress = tag.getFloat("Target");
        if (clientPacket)
            smoothProgress.chase(progress, 0.1, LerpedFloat.Chaser.LINEAR);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("Progress", progress);
        tag.putFloat("Target", targetProgress);
    }

    static class ValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 2, 14);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return super.getLocalOffset(level, pos, state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal() && !state.getValue(VariacBlock.HORIZONTAL_FACING).equals(direction);
        }
    }

    static double[] ratios = new double[]{
            0.1d,
            0.2d,
            0.3d,
            0.4d,
            0.5d,
            0.6d,
            0.7d,
            0.9d,
            1.0d,
            1.1d,
            1.2d,
            1.3d,
            1.4d,
            1.5d,
            1.6d,
            1.7d,
            1.9d,
            2.0d,
    };
}
