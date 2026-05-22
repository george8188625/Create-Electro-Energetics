package com.george_vi.electroenergetics.foundation;

import com.george_vi.electroenergetics.CEEFluids;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;

public interface ProperOilAndWaterloggedBlock extends BucketPickup, LiquidBlockContainer {

    EnumProperty<LoggedState> LOGGED_STATE = EnumProperty.create("logged_state", LoggedState.class);

    default FluidState fluidState(BlockState state) {
        return state.getValue(LOGGED_STATE).getFluidState();
    }

    default void updateWater(LevelAccessor level, BlockState state, BlockPos pos) {
        LoggedState loggedState = state.getValue(LOGGED_STATE);
        if (loggedState.isWet()) {
            Fluid fluid = loggedState.getFluid();
            level.scheduleTick(pos, fluid, fluid.getTickDelay(level));
        }
    }

    default BlockState withWater(BlockState placementState, BlockPlaceContext ctx) {
        return withWater(ctx.getLevel(), placementState, ctx.getClickedPos());
    }

    static BlockState withWater(LevelAccessor level, BlockState placementState, BlockPos pos) {
        if (placementState == null)
            return null;
        FluidState ifluidstate = level.getFluidState(pos);
        if (placementState.isAir())
            return ifluidstate.getType() == Fluids.WATER ? ifluidstate.createLegacyBlock() : placementState;
        if (!(placementState.getBlock() instanceof ProperOilAndWaterloggedBlock))
            return placementState;
        return placementState.setValue(LOGGED_STATE,
                ifluidstate.getType() == Fluids.WATER ? LoggedState.WATERLOGGED :
                ifluidstate.getType() == CEEFluids.TRANSFORMER_OIL.getSource() ? LoggedState.OILLOGGED :
                LoggedState.DRY
        );
    }

    @Override
    default boolean canPlaceLiquid(@Nullable Player player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return fluid == Fluids.WATER || fluid == CEEFluids.TRANSFORMER_OIL.getSource();
    }

    @Override
    default boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        LoggedState loggedState = state.getValue(LOGGED_STATE);
        if (loggedState != LoggedState.WATERLOGGED && fluidState.getType() == Fluids.WATER) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(LOGGED_STATE, LoggedState.WATERLOGGED), 3);
                level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
            }

            return true;
        } else if (loggedState != LoggedState.OILLOGGED && fluidState.getType() == CEEFluids.TRANSFORMER_OIL.getSource()) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(LOGGED_STATE, LoggedState.OILLOGGED), 3);
                level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
            }

            return true;
        }
        return false;
    }


    @Override
    default ItemStack pickupBlock(@Nullable Player player, LevelAccessor level, BlockPos pos, BlockState state) {
        LoggedState loggedState = state.getValue(LOGGED_STATE);
        if (loggedState == LoggedState.DRY)
            return ItemStack.EMPTY;
        level.setBlock(pos, state.setValue(LOGGED_STATE, LoggedState.DRY), 3);

        if (loggedState == LoggedState.WATERLOGGED) {
            return new ItemStack(Items.WATER_BUCKET);
        } else if (loggedState == LoggedState.OILLOGGED) {
            return new ItemStack(CEEFluids.TRANSFORMER_OIL.getBucket().orElseThrow());
        }
        return ItemStack.EMPTY;
    }

    @Override
    default Optional<SoundEvent> getPickupSound() {
        return Fluids.WATER.getPickupSound();
    }

    enum LoggedState implements StringRepresentable {
        DRY,
        WATERLOGGED,
        OILLOGGED;


        @Override
        public @NotNull String getSerializedName() {
            return Lang.asId(name());
        }

        public boolean isWet() {
            return this != DRY;
        }

        public FluidState getFluidState() {
            return switch (this) {
                case OILLOGGED -> CEEFluids.TRANSFORMER_OIL.getSource().defaultFluidState();
                case WATERLOGGED -> Fluids.WATER.getSource(false);
                default -> Fluids.EMPTY.defaultFluidState();
            };
        }

        public Fluid getFluid() {
            return switch (this) {
                case OILLOGGED -> CEEFluids.TRANSFORMER_OIL.getSource();
                case WATERLOGGED -> Fluids.WATER;
                default -> Fluids.EMPTY;
            };
        }
    }
}
