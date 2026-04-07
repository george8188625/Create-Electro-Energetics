package com.george_vi.electroenergetics.content.connector;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.content.transmission_distribution.voltage_regulator.VoltageRegulatorBlock;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.NodeConfigurator;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DoubleConnectorBlock extends DirectionalRolledDeviceBlock<DoubleConnectorDevice> implements IWrenchable {
    public static final EnumProperty<Style> STYLE = EnumProperty.create("style", Style.class);

    public DoubleConnectorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(STYLE, Style.SHORT));
    }

    @Override
    public SimulatedDeviceType<DoubleConnectorDevice> getDevice() {
        return CEESimulatedDevices.DOUBLE_CONNECTOR.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STYLE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(ROLL) ? state.getValue(STYLE).shaperRoll : state.getValue(STYLE).shaper)
                .get(state.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);
        return state;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return state.getValue(ROLL) ?
                state.getValue(STYLE).nodeConfigurator.rotate(new Vec3(0, 90, 0))
                        .getNodes(state.getValue(FACING)) :
                state.getValue(STYLE).nodeConfigurator.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return state.getValue(ROLL) ?
                state.getValue(STYLE).nodeConfigurator.rotate(new Vec3(0, 90, 0))
                        .getNodePos(state.getValue(FACING), id) :
                state.getValue(STYLE).nodeConfigurator.getNodePos(state.getValue(FACING), id);
    }

    @Override
    protected @NotNull VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return targetedFace.getAxis() == originalState.getValue(FACING).getAxis() ? originalState.cycle(ROLL) :
                originalState.cycle(STYLE);
    }

    @Override
    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(FACING) == Direction.UP) {
            BlockPos posBelow = pos.below();
            BlockState stateBelow = level.getBlockState(posBelow);
            if (CEEBlocks.VOLTAGE_REGULATOR.has(stateBelow) &&
                    state.getValue(DoubleConnectorBlock.ROLL) == (stateBelow.getValue(VoltageRegulatorBlock.FACING).getAxis() == Direction.Axis.X)) {
                return id == (stateBelow.getValue(VoltageRegulatorBlock.FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0) ?
                        CEELang.nodeLabel("input") : CEELang.nodeLabel("output");
            }
        }
        return super.getNodeLabel(level, pos, state, id);
    }

    @Override
    public boolean canSelfConnect(Level level, BlockPos pos, BlockState state, int id1, int id2) {
        return true;
    }

    public enum Style implements StringRepresentable {
        SHORT("", CEENodeConfigurations.DOUBLE_CONNECTOR,
                CEEShapes.DOUBLE_CONNECTOR, CEEShapes.DOUBLE_CONNECTOR_ROLL),
        MEDIUM("medium", CEENodeConfigurations.DOUBLE_CONNECTOR_MEDIUM,
                CEEShapes.DOUBLE_CONNECTOR_MEDIUM, CEEShapes.DOUBLE_CONNECTOR_MEDIUM_ROLL),
        LARGE("large", CEENodeConfigurations.DOUBLE_CONNECTOR_LARGE,
                CEEShapes.DOUBLE_CONNECTOR_LARGE, CEEShapes.DOUBLE_CONNECTOR_LARGE_ROLL),
        LARGE_ANGLED("large_angled", CEENodeConfigurations.DOUBLE_CONNECTOR_LARGE_ANGLED,
                CEEShapes.DOUBLE_CONNECTOR_LARGE_ANGLED, CEEShapes.DOUBLE_CONNECTOR_LARGE_ANGLED_ROLL);

        public final String suffix;
        public final NodeConfigurator nodeConfigurator;
        public final VoxelShaper shaper;
        public final VoxelShaper shaperRoll;

        Style(String suffix, NodeConfigurator nodeConfigurator, VoxelShaper shaper, VoxelShaper shaperRoll) {
            this.suffix = suffix;
            this.nodeConfigurator = nodeConfigurator;
            this.shaper = shaper;
            this.shaperRoll = shaperRoll;
        }

        @Override
        public String getSerializedName() {
            return Lang.asId(name());
        }
    }
}
