package com.george_vi.electroenergetics.content.railway_electrification.catenary;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.content.connector.ConnectorDevice;
import com.george_vi.electroenergetics.foundation.base.SimpleElectricalDeviceBlock;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CatenaryHolderBlock extends SimpleElectricalDeviceBlock<ConnectorDevice> implements IBE<CatenaryHolderBlockEntity>, ProperWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<Style> STYLE = EnumProperty.create("style", Style.class);

    public CatenaryHolderBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false).setValue(STYLE, Style.STANDARD));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, STYLE);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return withWater(defaultBlockState(), context);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);
        return state;
    }

    @Override
    public SimulatedDeviceType<ConnectorDevice> getDevice() {
        return CEESimulatedDevices.CONNECTOR.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(STYLE).isLow() ?
                CEEShapes.CATENARY_HOLDER_LOW.get(Direction.Axis.Y) :
                CEEShapes.CATENARY_HOLDER.get(Direction.Axis.Y);
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return Map.of(0, new Vec3(8/16f, (state.getValue(STYLE).isLow() ? 0.5 : 22)/16f, 8/16f));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return new Vec3(8/16f, (state.getValue(STYLE).isLow() ? 0.5 : 22)/16f, 8/16f);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return originalState.cycle(STYLE);
    }

    @Override
    public Class<CatenaryHolderBlockEntity> getBlockEntityClass() {
        return CatenaryHolderBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CatenaryHolderBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.CATENARY_HOLDER.get();
    }

    public static enum Style implements StringRepresentable {
        STANDARD, WEATHERED, LOW, WEATHERED_LOW;

        @Override
        public @NotNull String getSerializedName() {
            return Lang.asId(name());
        }

        public boolean isLow() {
            return this == LOW || this == WEATHERED_LOW;
        }

        public boolean isWeathered() {
            return this == WEATHERED || this == WEATHERED_LOW;
        }
    }
}
