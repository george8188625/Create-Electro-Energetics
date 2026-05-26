package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.ProperOilAndWaterloggedBlock;
import com.george_vi.electroenergetics.foundation.base.DirectionalKineticElectricBlock;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class AlternatorBrushesBlock extends DirectionalKineticElectricBlock<AlternatorBrushesDevice> implements IBE<AlternatorBrushesBlockEntity>, ProperOilAndWaterloggedBlock {
    public static final BooleanProperty ROLL = DirectionalRolledDeviceBlock.ROLL;
    public static final EnumProperty<ProperOilAndWaterloggedBlock.LoggedState> LOGGED_STATE = ProperOilAndWaterloggedBlock.LOGGED_STATE;

    public AlternatorBrushesBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(LOGGED_STATE, ProperOilAndWaterloggedBlock.LoggedState.DRY)
                .setValue(ROLL, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LOGGED_STATE, ROLL);
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredFacing(context);
        if (preferred == null || context.getPlayer().isShiftKeyDown())
            preferred = context.getNearestLookingDirection();
        if (!context.getPlayer().isShiftKeyDown())
            preferred = preferred.getOpposite();

        if (preferred.getAxis().isVertical())
            return withWater(defaultBlockState().setValue(FACING, preferred).setValue(ROLL, context.getHorizontalDirection().getAxis() == Direction.Axis.X), context);

        return withWater(defaultBlockState().setValue(FACING, preferred), context);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.ALTERNATOR_BRUSHES.get(state.getValue(FACING));
    }

    @Override
    public SimulatedDeviceType<AlternatorBrushesDevice> getDevice() {
        return CEESimulatedDevices.ALTERNATOR_BRUSHES.get();
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return CEENodeConfigurations.ALTERNATOR_BRUSHES.getNodes(state.getValue(FACING), state.getValue(ROLL));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return CEENodeConfigurations.ALTERNATOR_BRUSHES.getNodePos(state.getValue(FACING), state.getValue(ROLL), id);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        if (targetedFace.getAxis() == originalState.getValue(FACING).getAxis())
            return originalState.cycle(ROLL);
        return super.getRotatedBlockState(originalState, targetedFace);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        if (state.getValue(FACING).getAxis() == Direction.Axis.Y)
            if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90)
                return state.cycle(ROLL);
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }


    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public Class<AlternatorBrushesBlockEntity> getBlockEntityClass() {
        return AlternatorBrushesBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AlternatorBrushesBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.ALTERNATOR_BRUSHES.get();
    }
}
