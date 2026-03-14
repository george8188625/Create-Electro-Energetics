package com.george_vi.electroenergetics.content.cut_off_switch;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MomentarySwitchDevice extends SimulatedDevice<MomentarySwitchDevice.DataHolder> {

    public MomentarySwitchDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        double r = extraData.behaviour.resistance();
        if (r != 0)
            bridges.builder(pos).resistor(0, 1, r);

    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        Vec3 pPos = null;
        if (level.isLoaded(pos)) {
            pPos = Vec3.atCenterOf(pos);
            BlockState blockState = level.getBlockState(pos);
            pPos = pPos.subtract(Vec3.atLowerCornerOf(blockState.getValue(CutOffSwitchBlock.FACING).getNormal()).multiply(0.125, 0.125, 0.125));
            if (extraData.closedTicks == 0) {
                if (blockState.getBlock() instanceof MomentarySwitchBlock block)
                    block.openSwitch(blockState, (ServerLevel) level, pos);
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.1f, 1);
            }
        }

        extraData.closedTicks = Math.max(-1, extraData.closedTicks - 1);
        extraData.behaviour.isClosed = extraData.closedTicks > 0;
        extraData.behaviour.postTick(results.getVoltageAt(pos, 0, 1), pPos, level);
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.closedTicks = tag.getInt("ClosedTicks");
        dataHolder.behaviour = new SwitchingBehaviour(tag.getCompound("Behaviour"));
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("ClosedTicks", extraData.closedTicks);
        tag.put("Behaviour", extraData.behaviour.write());
        return tag;
    }

    public static class DataHolder {
        public SwitchingBehaviour behaviour;
        public int closedTicks;
    }
}
