package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.base.DirectionalKineticElectricBlock;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class ThreePhaseAlternatorBrushesBlock extends DirectionalKineticElectricBlock<ThreePhaseAlternatorBrushesDevice> implements IBE<AlternatorBrushesBlockEntity> {

    public ThreePhaseAlternatorBrushesBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.ALTERNATOR_BRUSHES.get(state.getValue(FACING));
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return CEENodeConfigurations.THREE_PHASE_BRUSH.getNodes(state.getValue(FACING).getOpposite());
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return CEENodeConfigurations.THREE_PHASE_BRUSH.getNodePos(state.getValue(FACING).getOpposite(), id);
    }

    @Override
    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        return id == 0 ? CEELang.nodeLabel("neutral") :
                id == 1 ? CEELang.nodeLabel("phase_1") :
                id == 2 ? CEELang.nodeLabel("phase_2") :
                        CEELang.nodeLabel("phase_3");
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public SimulatedDeviceType<ThreePhaseAlternatorBrushesDevice> getDevice() {
        return CEESimulatedDevices.THREE_PHASE_ALTERNATOR_BRUSHES.get();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!(level instanceof ServerLevel sl))
            return;

        ThreePhaseAlternatorBrushesDevice data = DevicesSavedData.load(sl).getDevice(pos, ThreePhaseAlternatorBrushesDevice.class);
        if (data == null)
            return;
        Direction fastDirection = state.getValue(FACING).getAxis().isVertical() ? Direction.EAST : state.getValue(FACING).getClockWise();
        data.fast = level.hasSignal(pos.relative(fastDirection), fastDirection.getOpposite());
        Direction slowDirection = state.getValue(FACING).getAxis().isVertical() ? Direction.WEST : state.getValue(FACING).getCounterClockWise();
        data.slow = level.hasSignal(pos.relative(slowDirection), slowDirection.getOpposite());
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
