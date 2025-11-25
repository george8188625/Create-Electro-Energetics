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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CutOffSwitchDevice extends SimulatedDevice<CutOffSwitchDevice.DataHolder> {
    final int lines;
    public CutOffSwitchDevice(ResourceLocation id, int lines) {
        super(id);
        this.lines = lines;
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        for (int i = 0; i < lines; i++) {
            SwitchState state = extraData.states[i];
            if (state == SwitchState.CLOSED)
                    bridges.builder(pos).resistor(i, (lines) + i, 0.001);

            if (state == SwitchState.ARCING)
                    bridges.builder(pos).resistor(i, (lines) + i, 400);
            if (state == SwitchState.OPENING)
                bridges.builder(pos).resistor(i, (lines) + i, 10000);
        }
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        extraData.isArcing = false;
        for (int i = 0; i < lines; i++) {
            SwitchState state = extraData.states[i];
            if (extraData.isClosed)
                state = SwitchState.CLOSED;
            if ((!extraData.isClosed && state == SwitchState.OPENING) || state == SwitchState.ARCING) {
                if (Math.abs(results.getVoltageAt(pos, i) - results.getVoltageAt(pos, lines + i)) > (state == SwitchState.ARCING ? 60 : 1000)) {
                    if (state != SwitchState.ARCING)
                        extraData.tick = 10;
                    state = SwitchState.ARCING;
                } else
                    state = SwitchState.OPEN;
            }
            if (!extraData.isClosed && state == SwitchState.CLOSED)
                state = SwitchState.OPENING;

            if (state == SwitchState.ARCING)
                extraData.isArcing = true;
            extraData.states[i] = state;
        }

        if (extraData.isArcing)
            if (level.isLoaded(pos)) {
                Vec3 pPos = Vec3.atCenterOf(pos);
                BlockState blockState = level.getBlockState(pos);
                pPos = pPos.subtract(Vec3.atLowerCornerOf(blockState.getValue(CutOffSwitchBlock.FACING).getNormal()).multiply(0.125, 0.125, 0.125));
                if (level.random.nextFloat() > 0.5)
                    ((ServerLevel)level).sendParticles(ParticleTypes.BUBBLE_POP, pPos.x, pPos.y, pPos.z, 10, 0.05, 0.05, 0.05, 0);

                if (extraData.tick >= 10) {
                    level.playSound(null, pPos.x, pPos.y, pPos.z, CEESoundEvents.SHORT_ARC.get(), SoundSource.BLOCKS, 0.7f, 1f);
                    extraData.tick = 0;
                }
                extraData.tick++;
            }
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.tick = tag.getInt("Tick");
        dataHolder.isArcing = tag.getBoolean("Arcing");
        dataHolder.isClosed = tag.getBoolean("Closed");
        dataHolder.states = new SwitchState[lines];
        for (int i = 0; i < lines; i++)
            dataHolder.states[i] = SwitchState.values()[tag.getInt("State_"+i)];
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Tick", extraData.tick);
        if (extraData.isArcing)
            tag.putBoolean("Arcing", true);
        if (extraData.isClosed)
            tag.putBoolean("Closed", true);
        for (int i = 0; i < lines; i++)
            tag.putInt("State_" + i, extraData.states[i].ordinal());
        return tag;
    }

    public static class DataHolder {
        public SwitchState[] states;
        public boolean isArcing;
        public boolean isClosed;
        public int tick;
    }
    enum SwitchState {
        OPEN,
        CLOSED,
        OPENING,
        ARCING;
    }
}
