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

            SwitchingBehaviour behaviour = extraData.behaviours[i];
            double r = behaviour.resistance();
            if (r != 0)
                bridges.builder(pos).resistor(i, (lines) + i, r);
        }
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        Vec3 pPos = null;
        if (level.isLoaded(pos)) {
            pPos = Vec3.atCenterOf(pos);
            BlockState blockState = level.getBlockState(pos);
            pPos = pPos.subtract(Vec3.atLowerCornerOf(blockState.getValue(CutOffSwitchBlock.FACING).getNormal()).multiply(0.125, 0.125, 0.125));
        }

        for (int i = 0; i < lines; i++) {
            SwitchingBehaviour behaviour = extraData.behaviours[i];
            behaviour.isClosed = extraData.isClosed;
            behaviour.postTick(results.getVoltageAt(pos, i, lines + i), pPos, level);
        }
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.behaviours = new SwitchingBehaviour[lines];
        dataHolder.isClosed = tag.getBoolean("Closed");
        for (int i = 0; i < lines; i++)
            dataHolder.behaviours[i] = new SwitchingBehaviour(tag.getCompound("Switch_"+i));
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        if (extraData.isClosed)
            tag.putBoolean("Closed", true);
        for (int i = 0; i < lines; i++)
            tag.put("Switch_"+i, extraData.behaviours[i].write());
        return tag;
    }

    public static class DataHolder {
        public SwitchingBehaviour[] behaviours;
        public boolean isClosed;
    }
}
