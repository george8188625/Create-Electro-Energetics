package com.george_vi.electroenergetics.content.electronic_components.diode;

import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.foundation.base.SimpleElectricalDeviceBlock;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DiodeBlock extends DirectionalRolledDeviceBlock<DiodeDevice> implements IWrenchable, ProperWaterloggedBlock {
    public static final BooleanProperty FLIP = BooleanProperty.create("flip");

    public DiodeBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FLIP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FLIP);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null && context.getHorizontalDirection().getAxisDirection() == Direction.AxisDirection.POSITIVE)
            state = state.cycle(FLIP);
        return state;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(ROLL) ? CEEShapes.ELECTRONIC_8_ROLL.get(state.getValue(FACING)) :
                CEEShapes.ELECTRONIC_8.get(state.getValue(FACING));
    }

    @Override
    public SimulatedDeviceType<DiodeDevice> getDevice() {
        return CEESimulatedDevices.DIODE.get();
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        float rot = 0;
        if (state.getValue(FLIP))
            rot += 180;
        if (state.getValue(ROLL))
            rot += 90;
        return CEENodeConfigurations.ELECTRONIC_8.rotate(new Vec3(0, rot, 0)).getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        float rot = 0;
        if (state.getValue(FLIP))
            rot += 180;
        if (state.getValue(ROLL))
            rot += 90;
        return CEENodeConfigurations.ELECTRONIC_8.rotate(new Vec3(0, rot, 0)).getNodePos(state.getValue(FACING), id);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        BlockState state = super.getRotatedBlockState(originalState, targetedFace);
        if (targetedFace.getAxis() == originalState.getValue(FACING).getAxis() && state.getValue(ROLL))
            state = state.cycle(FLIP);
        return state;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        return CEELang.nodeLabel(id == 0 ? "cathode" : "anode");
    }
}
