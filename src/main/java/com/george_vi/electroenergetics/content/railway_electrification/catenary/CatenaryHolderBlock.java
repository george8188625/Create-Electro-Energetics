package com.george_vi.electroenergetics.content.railway_electrification.catenary;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class CatenaryHolderBlock extends SimpleDeviceBlock implements IBE<CatenaryHolderBlockEntity> {
    public CatenaryHolderBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.CONNECTOR;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.FOUR_VOXEL_POLE.get(Direction.Axis.Y);
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return Map.of(0, new Vec3(8/16f, 14/16f, 8/16f));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return new Vec3(8/16f, 14/16f, 8/16f);
    }

    @Override
    public Class<CatenaryHolderBlockEntity> getBlockEntityClass() {
        return CatenaryHolderBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CatenaryHolderBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.CATENARY_HOLDER.get();
    }
}
