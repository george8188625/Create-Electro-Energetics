package com.george_vi.electroenergetics.content.bulb;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.config.CEEServer;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.Node;
import com.george_vi.electroenergetics.simulation.NodeConnection;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
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
        if (!extraData.getBoolean("destroyed"))
            bridges.builder(pos).resistor(0, 1, CEEConfigs.server().bulbResistance.get());
    }

    @Override
    public void postTick(BlockPos pos, Level level, Map<Node, Double> voltages, Map<NodeConnection, Double> sourceAmps, CompoundTag extraData) {
        if (voltages.size() != 2)
            return;
        double vd = Math.abs(voltages.get(new Node(0, pos)) - voltages.get(new Node(1, pos)));
        if (level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            if (extraData.getBoolean("destroyed")) {
                if (CEEBlocks.BULB.has(state)) {
                    level.setBlockAndUpdate(pos, CEEBlocks.BROKEN_BULB.get().withPropertiesOf(state));
                    Vec3 pPos = Vec3.atCenterOf(pos);
                    ((ServerLevel)level).sendParticles(ParticleTypes.BUBBLE_POP, pPos.x, pPos.y, pPos.z, 15, 0.06, 0.06, 0.06, 0);
                }
                return;
            }

            extraData.putDouble("lastVoltage", vd);

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
        if (vd / CEEConfigs.server().bulbResistance.get() > CEEConfigs.server().bulbBreakAmperage.get())
            extraData.putBoolean("destroyed", true);
    }

    private int getLightLevel(double vd) {
        return (int) Math.min(Math.round(vd / 100), 2);
    }
}
