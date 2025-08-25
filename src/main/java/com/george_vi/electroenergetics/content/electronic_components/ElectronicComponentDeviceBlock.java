package com.george_vi.electroenergetics.content.electronic_components;

import com.george_vi.electroenergetics.foundation.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class ElectronicComponentDeviceBlock extends SimpleDeviceBlock {
    public ElectronicComponentDeviceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected SimulatedDevice getDevice() {
        return null;
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return null;
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return null;
    }
}
