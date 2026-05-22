package com.george_vi.electroenergetics.content.bundled_wire;

import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BundledWireTerminationBlock extends DirectionalRolledDeviceBlock<BundledWireTerminationDevice> {
    public static final BooleanProperty FLIP = BooleanProperty.create("flip");
    public final BundledWireType wireType;

    public BundledWireTerminationBlock(Properties properties, BundledWireType type) {
        super(properties);
        this.wireType = type;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FLIP);
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return Map.of();
    }

    @Override
    public @Nullable Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return null;
    }

    @Override
    public SimulatedDeviceType<BundledWireTerminationDevice> getDevice() {
        return CEESimulatedDevices.BUNDLED_WIRE_TERMINATION.get();
    }
}

