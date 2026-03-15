package com.george_vi.electroenergetics.content.transmission_distribution.transformer;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
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

import java.util.Optional;

public class TransformerCoreDevice extends SimulatedDevice<TransformerCoreDevice.DataHolder> {
    public TransformerCoreDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        Direction facing = extraData.facing;
        if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
            return; // the block with positive facing is the controller

        double ratio = extraData.ratio;
        if (ratio == 0)
            ratio = 1;
        BlockPos otherPos = pos.relative(facing);
        InWorldNode[] nodes = new InWorldNode[] {new InWorldNode(0, pos), new InWorldNode(1, pos), new InWorldNode(0, otherPos), new InWorldNode(1, otherPos), new InWorldNode(2, pos), new InWorldNode(3, pos)};

        TransformerBehaviour.preTick(nodes, ratio, pos, bridges, extraData.transformerData);

    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        Direction facing = extraData.facing;
        if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
            return; // the block with positive facing is the controller

        BlockPos otherPos = pos.relative(facing);
        InWorldNode[] nodes = new InWorldNode[] {new InWorldNode(0, pos), new InWorldNode(1, pos), new InWorldNode(0, otherPos), new InWorldNode(1, otherPos), new InWorldNode(2, pos), new InWorldNode(3, pos)};

        double power = TransformerBehaviour.postTick(nodes, results, extraData.transformerData);

        if (extraData.be == null)
            if (level.getBlockEntity(pos) instanceof TransformerCoreBlockEntity be)
                extraData.be = be;

        if (extraData.be != null) {
            if (extraData.be.isRemoved())
                extraData.be = null;
            else {
                extraData.be.power = Math.abs(power);
                extraData.be.primaryVoltage = extraData.transformerData.lastPrimaryVoltage;
                extraData.be.secondaryVoltage = extraData.transformerData.lastSecondaryVoltage;
            }
        }
        double dissipationFactor = extraData.heatDissipation;

        double loadFactor = Math.abs(power) / (dissipationFactor == 0 ? 0.001 : dissipationFactor);
        loadFactor = Math.min(2.5, loadFactor);
        extraData.temp = updateTemp(extraData.temp, (float) loadFactor * 1000);

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (extraData.temp > 31_000) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.LARGE));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
            sd.removeDevice(pos);
            level.explode(null, Explosion.getDefaultDamageSource(level, null), new TransformerExplosionDamageCalculator(), pos.getX(), pos.getY(), pos.getZ(), 4, true, Level.ExplosionInteraction.BLOCK);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (extraData.temp > 26_000) {
            showOverheatingParticles(level, pos);
            showOverheatingParticles(level, pos.relative(facing));
        }

    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.temp = tag.getFloat("Temp");
        dataHolder.ratio = tag.getDouble("Ratio");
        dataHolder.transformerData = new TransformerBehaviour.TransformerBehaviourDataHolder(tag.getCompound("TransformerData"));
        dataHolder.facing = Direction.values()[tag.getByte("Facing")];
        dataHolder.heatDissipation = tag.getDouble("HeatDissipationFactor");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Temp", extraData.temp);
        tag.putDouble("Ratio", extraData.ratio);
        tag.putDouble("HeatDissipationFactor", extraData.heatDissipation);
        tag.put("TransformerData", extraData.transformerData.write());
        tag.putByte("Facing", (byte) extraData.facing.ordinal());
        return tag;
    }

    public static class DataHolder {
        public TransformerBehaviour.TransformerBehaviourDataHolder transformerData;
        public Direction facing;
        public float temp;
        public double ratio;
        public TransformerCoreBlockEntity be;
        public double heatDissipation;
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
