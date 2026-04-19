package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHolderBlock;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class WireEffects {

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused())
            return;

        for (Pair<InWorldNodeConnection, WireData> wire : WireRenderer.getAllConnections()) {
            InWorldNodeConnection connection = wire.getFirst();

            Vec3 pos1 = connection.node1().getPosition(mc.level);
            Vec3 pos2 = connection.node2().getPosition(mc.level);

            if (pos1 == null || pos2 == null) {
                pos1 = connection.node1().sourcePos().getCenter();
                pos2 = connection.node2().sourcePos().getCenter();
            }

            double distance = pos1.distanceTo(pos2);
            List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wire.getSecond().getSag(distance));

            spawnDrippingWater(points);
        }

        for (CatenaryConnection connection : WireRenderer.CATENARY) {
//            InWorldNodeConnection connection = wire.getFirst();
//
            Vec3 pos1 = connection.pos1().getBottomCenter();
            Vec3 pos2 = connection.pos2().getBottomCenter();

            List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, 0);

            spawnDrippingWater(points);

            BlockState startingState = mc.level.getBlockState(connection.pos1());
            BlockState endingState = mc.level.getBlockState(connection.pos2());

            boolean isStartingLow = CEEBlocks.CATENARY_HOLDER.has(startingState) && startingState.getValue(CatenaryHolderBlock.STYLE).isLow();
            boolean isEndingLow = CEEBlocks.CATENARY_HOLDER.has(endingState) && endingState.getValue(CatenaryHolderBlock.STYLE).isLow();
            if (isStartingLow || isEndingLow)
                continue;

            Vec3 topPos1 = pos1.add(0, 1.5, 0);
            Vec3 topPos2 = pos2.add(0, 1.5, 0);

            float distance = (float) topPos1.distanceTo(topPos2);

            List<Vec3> topPoints = QuadraticWireHelper.cablePoints(topPos1, topPos2, 350f * (0.05f / distance), 4);

            spawnDrippingWater(topPoints);
//            BlockState state1 = mc.level.getBlockState(connection.pos1().sourcePos());
//            BlockState state2 = mc.level.getBlockState(connection.pos2().sourcePos());
//
//            if (state1.getBlock() instanceof DeviceBlock db)
//                pos1 = db.getNodePosition(mc.level, connection.pos1().sourcePos(), state1, connection.pos1().id());
//            if (state2.getBlock() instanceof DeviceBlock db)
//                pos2 = db.getNodePosition(mc.level, connection.pos2().sourcePos(), state2, connection.pos2().id());
//
//            if (pos1 == null || pos2 == null)
//                continue;
//
//            BlockPos devicePos1 = connection.pos1().sourcePos();
//            BlockPos devicePos2 = connection.pos2().sourcePos();
//            pos1 = pos1.add(devicePos1.getX(), devicePos1.getY(), devicePos1.getZ());
//            pos2 = pos2.add(devicePos2.getX(), devicePos2.getY(), devicePos2.getZ());
//
//            List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wire.getSecond().wireType().getSag());
//
//            spawnDrippingWater(points);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnDrippingWater(List<Vec3> points) {
        Minecraft mc = Minecraft.getInstance();

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 point = points.get(i);
            Vec3 nextPoint = points.get(i + 1);
            if (point.distanceTo(mc.player.getEyePosition()) > 80)
                continue;

            boolean bubbles = false;
            if (mc.level.random.nextFloat() > 0.02) {
                if (mc.level.random.nextFloat() > 0.03)
                    continue;
                bubbles = true;
            }

            Vec3 pos = VecHelper.lerp(mc.level.random.nextFloat(), point, nextPoint);
            if (mc.level.isRainingAt(BlockPos.containing(pos))) {
                if (bubbles)
                    mc.level.addParticle(ParticleTypes.SPLASH, pos.x(), pos.y(), pos.z(), 0, 0, 0);
                else
                    mc.level.addParticle(ParticleTypes.DRIPPING_WATER, pos.x(), pos.y() - 0.1, pos.z(), 0, 0, 0);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void showSmallSpark(Vec3 pos) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        for (int i = 0; i < 15; i++) {
            Vec3 vel = Vec3.ZERO.offsetRandom(level.random, 0.06f);
            level.addParticle(ParticleTypes.BUBBLE_POP, pos.x, pos.y, pos.z, vel.z, vel.y, vel.z);
        }

        level.addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 0, 0, 0);
        level.addParticle(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y, pos.z, 0, 0, 0);
        ElectricHumSoundInstance instance = new ElectricHumSoundInstance(CEESoundEvents.ARC.get(), BlockPos.containing(pos));
        Minecraft.getInstance()
                .getSoundManager()
                .play(instance);
        instance.setVolume(0.3f);
    }

    @OnlyIn(Dist.CLIENT)
    public static void showMediumSpark(Vec3 pos) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        for (int i = 0; i < 15; i++) {
            Vec3 vel = Vec3.ZERO.offsetRandom(level.random, 0.1f);
            level.addParticle(ParticleTypes.BUBBLE_POP, pos.x, pos.y, pos.z, vel.z, vel.y, vel.z);
        }

        level.addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 0, 0, 0);
        level.addParticle(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y, pos.z, 0, 0, 0);
        ElectricHumSoundInstance instance = new ElectricHumSoundInstance(CEESoundEvents.ARC.get(), BlockPos.containing(pos));
        Minecraft.getInstance()
                .getSoundManager()
                .play(instance);
        instance.setVolume(1f);
    }

    @OnlyIn(Dist.CLIENT)
    public static void showLargeSpark(Vec3 pos) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        for (int i = 0; i < 32; i++) {
            Vec3 vel = Vec3.ZERO.offsetRandom(level.random, 0.25f);
            level.addParticle(ParticleTypes.BUBBLE_POP, pos.x, pos.y, pos.z, vel.z, vel.y, vel.z);
            vel = Vec3.ZERO.offsetRandom(level.random, 0.25f);
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y, pos.z, vel.z, vel.y, vel.z);
        }

        for (int i = 0; i < 32; i++) {
            Vec3 vel = Vec3.ZERO.offsetRandom(level.random, 0.25f);
            level.addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, vel.z, vel.y, vel.z);
        }

        level.addParticle(ParticleTypes.FLASH, pos.x, pos.y, pos.z, 0, 0, 0);
        level.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 0, 0, 0);
        ElectricHumSoundInstance instance = new ElectricHumSoundInstance(CEESoundEvents.ARC.get(), BlockPos.containing(pos));
        Minecraft.getInstance()
                .getSoundManager()
                .play(instance);
        instance.setVolume(1f);
    }
}
