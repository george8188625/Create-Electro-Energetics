package com.george_vi.electroenergetics.content.bulb;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class BulbDevice extends SimulatedDevice {
    public BulbDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        if (!extraData.getBoolean("Destroyed"))
            bridges.builder(pos).resistor(0, 1, CEEConfigs.server().resistanceValues.bulbResistance.get());
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        double vd = Math.abs(results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 1));
        if (level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            if (extraData.getBoolean("Destroyed")) {
                if (CEEBlocks.BULB.has(state)) {
                    level.setBlockAndUpdate(pos, CEEBlocks.BROKEN_BULB.get().withPropertiesOf(state));
                    Vec3 pPos = Vec3.atCenterOf(pos);
                    ((ServerLevel)level).sendParticles(ParticleTypes.BUBBLE_POP, pPos.x, pPos.y, pPos.z, 15, 0.06, 0.06, 0.06, 0);
                }
                return;
            }

            extraData.putDouble("LastVoltage", vd);

            int light = getLightLevel(vd);

            if (state.getBlock() instanceof BulbBlock) {
                int blockLight = state.getValue(BulbBlock.LIGHT);
                if (light == 2 && blockLight == 0)
                    light = 1;
                if (blockLight == 2 && light == 0)
                    light = 1;
                if (blockLight != light)
                    level.setBlockAndUpdate(pos, state.setValue(BulbBlock.LIGHT, light));
            }
        }
        if (vd / CEEConfigs.server().resistanceValues.bulbResistance.get() > CEEConfigs.server().bulbBreakAmperage.get())
            extraData.putBoolean("Destroyed", true);
    }

    private int getLightLevel(double vd) {
        return (int) Math.min(Math.round(vd / 100), 2);
    }
}
