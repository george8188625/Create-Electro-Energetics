package com.george_vi.electroenergetics.content.transformer;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.InWorldNode;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.Optional;

public class TransformerCoreDevice extends SimulatedDevice {
    public TransformerCoreDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        Direction facing = Direction.values()[extraData.getByte("Facing")];
        if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
            return; // the block with positive facing is the controller

        double ratio = extraData.getDouble("Ratio");
        if (ratio == 0)
            ratio = 1;
        BlockPos otherPos = pos.relative(facing);
        InWorldNode[] nodes = new InWorldNode[] {new InWorldNode(0, pos), new InWorldNode(1, pos), new InWorldNode(0, otherPos), new InWorldNode(1, otherPos), new InWorldNode(2, pos), new InWorldNode(3, pos)};

        TransformerBehaviour.preTick(nodes, ratio, pos, bridges, extraData);

    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        Direction facing = Direction.values()[extraData.getByte("Facing")];
        if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
            return; // the block with positive facing is the controller

        BlockPos otherPos = pos.relative(facing);
        InWorldNode[] nodes = new InWorldNode[] {new InWorldNode(0, pos), new InWorldNode(1, pos), new InWorldNode(0, otherPos), new InWorldNode(1, otherPos), new InWorldNode(2, pos), new InWorldNode(3, pos)};

        double power = TransformerBehaviour.postTick(nodes, results, extraData);

        float temp = updateTemp(extraData.getFloat("Temp"), (float) Math.abs(power) / 50);
        extraData.putFloat("Temp", temp);

        if (level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof TransformerCoreBlockEntity be)
                be.power = Math.abs(power);

        if (!CEEConfigs.server().componentDamage.get())
            return;

        double maxTemp = Math.max(0, 30 * (extraData.getDouble("HeatDissipationFactor") / 50 - 3.3));

        if (temp > maxTemp) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.BIG));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
            sd.removeDevice(pos);
            level.explode(null, Explosion.getDefaultDamageSource(level, null), new TransformerExplosionDamageCalculator(), pos.getX(), pos.getY(), pos.getZ(), 4, true, Level.ExplosionInteraction.BLOCK);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (temp > maxTemp * 0.75) {
            showOverheatingParticles(level, pos);
            showOverheatingParticles(level, pos.relative(facing));
        }

    }


    // Make the explosion destroy water, so that when a water-submerged transformer explodes, it actually explodes
    static class TransformerExplosionDamageCalculator extends ExplosionDamageCalculator {
        @Override
        public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, FluidState fluid) {
            if (fluid.isEmpty())
                return super.getBlockExplosionResistance(explosion, reader, pos, state, fluid);
            else
                return Optional.of(0.1f);
        }
    }
}
