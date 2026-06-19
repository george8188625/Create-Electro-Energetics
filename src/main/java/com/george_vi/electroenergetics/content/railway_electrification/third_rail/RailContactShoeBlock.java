package com.george_vi.electroenergetics.content.railway_electrification.third_rail;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.IPantographBlock;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographDevice;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographType;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.base.SimpleElectricalDeviceBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RailContactShoeBlock extends SimpleElectricalDeviceBlock<RailContactShoeDevice> implements IBE<RailContactShoeBlockEntity>, IPantographBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public RailContactShoeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getPlayer() != null && context.getPlayer().isShiftKeyDown() ? context.getHorizontalDirection() : context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.RAIL_CONTACT_SHOE.get(state.getValue(FACING));
    }

    @Override
    public PantographType getPantographType(BlockState state) {
        return CEEPantographTypes.RAIL_CONTACT_SHOE.get();
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public boolean isSidewaysPantograph() {
        return true;
    }

    @Override
    public Class<RailContactShoeBlockEntity> getBlockEntityClass() {
        return RailContactShoeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RailContactShoeBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.RAIL_CONTACT_SHOE.get();
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return CEENodeConfigurations.RAIL_CONTACT_SHOE.getNodes(state.getValue(FACING));
    }

    @Override
    public @Nullable Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return CEENodeConfigurations.RAIL_CONTACT_SHOE.getNodePos(state.getValue(FACING), id);
    }

    @Override
    public SimulatedDeviceType<RailContactShoeDevice> getDevice() {
        return CEESimulatedDevices.RAIL_CONTACT_SHOE.get();
    }
}
