package com.george_vi.electroenergetics.content.cut_off_switch;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class CutOffSwitchDevice extends SimulatedDevice {
    final int lines;
    public CutOffSwitchDevice(ResourceLocation id, int lines) {
        super(id);
        this.lines = lines;
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        for (int i = 0; i < lines; i++) {
            SwitchState state = SwitchState.values()[extraData.getInt("State_"+i)];
            if (state == SwitchState.CLOSED)
                    bridges.builder(pos).resistor(i, (lines) + i, 0.001);

            if (state == SwitchState.ARCING)
                    bridges.builder(pos).resistor(i, (lines) + i, 400);
            if (state == SwitchState.OPENING)
                bridges.builder(pos).resistor(i, (lines) + i, 10000);
        }
    }

    @Override
    public void postTick(BlockPos pos, Level level, Map<Node, Double> voltages, Map<NodeConnection, Double> sourceAmps, CompoundTag extraData) {
        if (voltages.size() != lines * 2)
            return;

        extraData.remove("Arcing");
        for (int i = 0; i < lines; i++) {
            SwitchState state = SwitchState.values()[extraData.getInt("State_"+i)];
            boolean closed = extraData.getBoolean("Closed");
            if (closed)
                state = SwitchState.CLOSED;
            if ((!closed && state == SwitchState.OPENING) || state == SwitchState.ARCING) {
                if (Math.abs(voltages.get(new Node(i, pos)) - voltages.get(new Node(lines + i, pos))) > (state == SwitchState.ARCING ? 60 : 1000)) {
                    if (state != SwitchState.ARCING)
                        extraData.putInt("Tick", 10);
                    state = SwitchState.ARCING;
                } else
                    state = SwitchState.OPEN;
            }
            if (!closed && state == SwitchState.CLOSED)
                state = SwitchState.OPENING;

            if (state == SwitchState.ARCING)
                extraData.putBoolean("Arcing", true);
            extraData.putInt("State_"+i, state.ordinal());

        }



        if (extraData.getBoolean("Arcing"))
            if (level.isLoaded(pos)) {
                Vec3 pPos = Vec3.atCenterOf(pos);
                BlockState blockState = level.getBlockState(pos);
                pPos = pPos.subtract(Vec3.atLowerCornerOf(blockState.getValue(CutOffSwitchBlock.FACING).getNormal()).multiply(0.125, 0.125, 0.125));
                if (level.random.nextFloat() > 0.5)
                    ((ServerLevel)level).sendParticles(ParticleTypes.BUBBLE_POP, pPos.x, pPos.y, pPos.z, 10, 0.05, 0.05, 0.05, 0);

                int tick = extraData.getInt("Tick");
                if (tick >= 10) {
                    level.playSound(null, pPos.x, pPos.y, pPos.z, CEESoundEvents.SHORT_ARC.get(), SoundSource.BLOCKS, 0.7f, 1f);
                    tick = 0;
                }
                tick++;
                extraData.putInt("Tick", tick);
            }
    }

    enum SwitchState {
        OPEN,
        CLOSED,
        OPENING,
        ARCING;
    }
}
