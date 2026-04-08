package com.george_vi.electroenergetics.content.transmission_distribution.transformer;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;

public class TransformerCoreDevice extends SimpleElectricalDevice {
    public TransformerBehaviour.TransformerBehaviourDataHolder transformerData;
    public Direction facing;
    public float temp;
    public double ratio;
    public TransformerCoreBlockEntity be;
    public double heatDissipation;

    public TransformerCoreDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
            return; // the block with positive facing is the controller

        if (ratio == 0)
            ratio = 1;
        BlockPos otherPos = pos.relative(facing);
        InWorldNode[] nodes = new InWorldNode[] {new InWorldNode(0, pos), new InWorldNode(1, pos),
                new InWorldNode(0, otherPos), new InWorldNode(1, otherPos),
                new InWorldNode(2, pos), new InWorldNode(3, pos)};

        TransformerBehaviour.preTick(nodes, ratio, pos, bridges, transformerData);

    }

    @Override
    public void postTick(SimulationResults results) {

        if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
            return; // the block with positive facing is the controller

        BlockPos otherPos = pos.relative(facing);
        InWorldNode[] nodes = new InWorldNode[] {new InWorldNode(0, pos), new InWorldNode(1, pos),
                new InWorldNode(0, otherPos), new InWorldNode(1, otherPos),
                new InWorldNode(2, pos), new InWorldNode(3, pos)};

        double power = TransformerBehaviour.postTick(nodes, results, transformerData);

        if (be == null)
            if (level.getBlockEntity(pos) instanceof TransformerCoreBlockEntity be)
                this.be = be;

        if (be != null) {
            if (be.isRemoved())
                be = null;
            else {
                be.power = Math.abs(power);
                be.primaryVoltage = transformerData.lastPrimaryVoltage;
                be.secondaryVoltage = transformerData.lastSecondaryVoltage;
            }
        }
        double dissipationFactor = heatDissipation;

        double loadFactor = Math.abs(power) / (dissipationFactor == 0 ? 0.001 : dissipationFactor);
        loadFactor = Math.min(2.5, loadFactor);
        temp = updateTemp(temp, (float) loadFactor * 1000);

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (temp > 31_000) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.LARGE));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            deviceSD.removeDevice(pos);
            level.explode(null, Explosion.getDefaultDamageSource(level, null), new TransformerExplosionDamageCalculator(), pos.getX(), pos.getY(), pos.getZ(), 4, true, Level.ExplosionInteraction.BLOCK);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (temp > 26_000) {
            showOverheatingParticles(level, pos);
            showOverheatingParticles(level, pos.relative(facing));
        }

    }

    @Override
    public void read(CompoundTag tag) {
        temp = tag.getFloat("Temp");
        ratio = tag.getDouble("Ratio");
        transformerData = new TransformerBehaviour.TransformerBehaviourDataHolder(tag.getCompound("TransformerData"));
        facing = Direction.values()[tag.getByte("Facing")];
        heatDissipation = tag.getDouble("HeatDissipationFactor");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putFloat("Temp", temp);
        tag.putDouble("Ratio", ratio);
        tag.putDouble("HeatDissipationFactor", heatDissipation);
        tag.put("TransformerData", transformerData.write());
        tag.putByte("Facing", (byte) facing.ordinal());
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
