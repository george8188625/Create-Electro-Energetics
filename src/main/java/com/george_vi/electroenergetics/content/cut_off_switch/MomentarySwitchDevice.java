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

public class MomentarySwitchDevice extends SimulatedDevice {

    public MomentarySwitchDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        CutOffSwitchDevice.SwitchState state = CutOffSwitchDevice.SwitchState.values()[extraData.getInt("State")];
        if (state == CutOffSwitchDevice.SwitchState.CLOSED)
            bridges.builder(pos).resistor(0, 1, 0.001);

        if (state == CutOffSwitchDevice.SwitchState.ARCING)
            bridges.builder(pos).resistor(0, 1, 400);
        if (state == CutOffSwitchDevice.SwitchState.OPENING)
            bridges.builder(pos).resistor(0, 1, 10000);

    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        extraData.remove("Arcing");
        CutOffSwitchDevice.SwitchState state = CutOffSwitchDevice.SwitchState.values()[extraData.getInt("State")];
        int closedTicks = extraData.getInt("ClosedTicks");
        boolean closed = closedTicks > 0;
        if (closed)
            state = CutOffSwitchDevice.SwitchState.CLOSED;
        if ((!closed && state == CutOffSwitchDevice.SwitchState.OPENING) || state == CutOffSwitchDevice.SwitchState.ARCING) {
            if (Math.abs(results.getVoltageAt(pos, 0, 1)) > (state == CutOffSwitchDevice.SwitchState.ARCING ? 60 : 1000)) {
                if (state != CutOffSwitchDevice.SwitchState.ARCING)
                    extraData.putInt("Tick", 10);
                state = CutOffSwitchDevice.SwitchState.ARCING;
            } else
                state = CutOffSwitchDevice.SwitchState.OPEN;
        }
        if (!closed && state == CutOffSwitchDevice.SwitchState.CLOSED)
            state = CutOffSwitchDevice.SwitchState.OPENING;

        if (state == CutOffSwitchDevice.SwitchState.ARCING)
            extraData.putBoolean("Arcing", true);
        extraData.putInt("State", state.ordinal());
        extraData.putInt("ClosedTicks", Math.max(-1, closedTicks - 1));

        if (level.isLoaded(pos)) {
            if (closedTicks == 0) {
                BlockState blockState = level.getBlockState(pos);
                if (blockState.getBlock() instanceof MomentarySwitchBlock block)
                    block.openSwitch(blockState, (ServerLevel) level, pos);
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.1f, 1);
            }

            if (extraData.getBoolean("Arcing")) {
                Vec3 pPos = Vec3.atCenterOf(pos);
                BlockState blockState = level.getBlockState(pos);
                pPos = pPos.subtract(Vec3.atLowerCornerOf(blockState.getValue(CutOffSwitchBlock.FACING).getNormal()).multiply(0.125, 0.125, 0.125));
                if (level.random.nextFloat() > 0.5)
                    ((ServerLevel) level).sendParticles(ParticleTypes.BUBBLE_POP, pPos.x, pPos.y, pPos.z, 10, 0.05, 0.05, 0.05, 0);

                int tick = extraData.getInt("Tick");
                if (tick >= 10) {
                    level.playSound(null, pPos.x, pPos.y, pPos.z, CEESoundEvents.SHORT_ARC.get(), SoundSource.BLOCKS, 0.7f, 1f);
                    tick = 0;
                }
                tick++;
                extraData.putInt("Tick", tick);
            }
        }
    }

    enum SwitchState {
        OPEN,
        CLOSED,
        OPENING,
        ARCING;
    }
}
