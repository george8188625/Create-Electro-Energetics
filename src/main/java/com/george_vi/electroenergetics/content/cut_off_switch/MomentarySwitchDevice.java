package com.george_vi.electroenergetics.content.cut_off_switch;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
        if (extraData.state == CutOffSwitchDevice.SwitchState.CLOSED)
            bridges.builder(pos).resistor(0, 1, 0.001);

        if (extraData.state == CutOffSwitchDevice.SwitchState.ARCING)
            bridges.builder(pos).resistor(0, 1, 400);
        if (extraData.state == CutOffSwitchDevice.SwitchState.OPENING)
            bridges.builder(pos).resistor(0, 1, 10000);

    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        extraData.isArcing = false;
        boolean closed = extraData.closedTicks > 0;
        if (closed)
            extraData.state = CutOffSwitchDevice.SwitchState.CLOSED;
        if ((!closed && extraData.state == CutOffSwitchDevice.SwitchState.OPENING) || extraData.state == CutOffSwitchDevice.SwitchState.ARCING) {
            if (Math.abs(results.getVoltageAt(pos, 0, 1)) > (extraData.state == CutOffSwitchDevice.SwitchState.ARCING ? 60 : 1000)) {
                if (extraData.state != CutOffSwitchDevice.SwitchState.ARCING)
                    extraData.tick = 10;
                extraData.state = CutOffSwitchDevice.SwitchState.ARCING;
            } else
                extraData.state = CutOffSwitchDevice.SwitchState.OPEN;
        }
        if (!closed && extraData.state == CutOffSwitchDevice.SwitchState.CLOSED)
            extraData.state = CutOffSwitchDevice.SwitchState.OPENING;

        if (extraData.state == CutOffSwitchDevice.SwitchState.ARCING)
            extraData.isArcing = true;
        extraData.closedTicks = Math.max(-1, extraData.closedTicks - 1);

        if (level.isLoaded(pos)) {
            if (extraData.closedTicks == 0) {
                BlockState blockState = level.getBlockState(pos);
                if (blockState.getBlock() instanceof MomentarySwitchBlock block)
                    block.openSwitch(blockState, (ServerLevel) level, pos);
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.1f, 1);
            }

            if (extraData.isArcing) {
                Vec3 pPos = Vec3.atCenterOf(pos);
                BlockState blockState = level.getBlockState(pos);
                pPos = pPos.subtract(Vec3.atLowerCornerOf(blockState.getValue(CutOffSwitchBlock.FACING).getNormal()).multiply(0.125, 0.125, 0.125));
                if (level.random.nextFloat() > 0.5)
                    ((ServerLevel) level).sendParticles(ParticleTypes.BUBBLE_POP, pPos.x, pPos.y, pPos.z, 10, 0.05, 0.05, 0.05, 0);

                if (extraData.tick >= 10) {
                    level.playSound(null, pPos.x, pPos.y, pPos.z, CEESoundEvents.SHORT_ARC.get(), SoundSource.BLOCKS, 0.7f, 1f);
                    extraData.tick = 0;
                }
                extraData.tick++;
            }
        }
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.tick = tag.getInt("Tick");
        dataHolder.isArcing = tag.getBoolean("Arcing");
        dataHolder.isClosed = tag.getBoolean("Closed");
        dataHolder.closedTicks = tag.getInt("ClosedTicks");
        dataHolder.state = CutOffSwitchDevice.SwitchState.values()[tag.getInt("State")];
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Tick", extraData.tick);
        tag.putInt("ClosedTicks", extraData.closedTicks);
        tag.putInt("State", extraData.state.ordinal());
        return tag;
    }

    public static class DataHolder {
        public CutOffSwitchDevice.SwitchState state;
        public boolean isArcing;
        public boolean isClosed;
        public int tick;
        public int closedTicks;
    }
}
