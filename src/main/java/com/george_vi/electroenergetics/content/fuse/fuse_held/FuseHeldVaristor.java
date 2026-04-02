package com.george_vi.electroenergetics.content.fuse.fuse_held;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.fuse.FuseHolderBlock;
import com.george_vi.electroenergetics.content.fuse.FuseHolderBlockEntity;
import com.george_vi.electroenergetics.content.varistor.VaristorProperties;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FuseHeldVaristor extends FuseHoldable {

    VaristorProperties properties = new VaristorProperties();
    //todo ponder
    @Override
    public void preTick(CompoundTag data, int id1, int id2, BridgeCollector.Builder builder, Level level, BlockPos pos) {
        properties.voltageAtOneAmp=300;
        if (data.contains("VoltageAtVaristor")) {
            properties.voltageAtContact = data.getDouble("VoltageAtVaristor");
        } else {
            properties.voltageAtContact=0;
        }

        /*For some reason that is beyond me using properties with connect() does not use up-to-date VoltageAtVaristor*/
        builder.resistor(
                id1,
                id2,
                properties.tickVaristor(
                        data.getDouble("VoltageAtVaristor"),
                        300
                )
        );


    }

    @Override
    public void postTick(CompoundTag data, int id1, int id2, SimulationResults results, Level level, BlockPos pos) {
        data.putDouble("VoltageAtVaristor", results.getVoltageAt(pos, id1));

        if (results.getVoltageAt(pos, id1, id2) >= CEEConfigs.server().voltageValues.varistorVoltage.get()){
            removeFuse(level, pos);
        }

        double current = Math.abs(results.getCurrentThrough(pos, id1, id2));
        float temp = data.getFloat("Temp");

        float newTemp = (float) (Math.min(current, 500));
        newTemp *= Math.min(temp < 0 ? 0 : 1 / (1 + (temp / 1000)), 1);
        newTemp = Math.max(temp - 33.3f + newTemp, 0);
        temp = newTemp;

        data.putFloat("Temp", temp);

        if (temp > CEEWireTypes.IRON.get().getMaxTemperature() * 2 / 3) {
                if (level.isLoaded(pos)) {
                    removeFuse(level, pos);
                }
                data.putBoolean("UpdateThisTick", true);
            }
    }

    private void removeFuse(Level level,BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof FuseHolderBlock && level.getBlockEntity(pos) instanceof FuseHolderBlockEntity be) {
            if (be.getFuseData().getFirst() != null && be.getFuseData().getFirst().getFirst() == this) {
                be.removeFuse(true, false);
            }
            if (be.getFuseData().getSecond() !=null && be.getFuseData().getSecond().getFirst() == this) {
                be.removeFuse(false, true);
            }
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }

            be.updateFuses();
        }
    }

    @Override
    public NonNullList<ItemStack> getDrops(CompoundTag data) {
        return NonNullList.of(Items.AIR.getDefaultInstance(), CEEBlocks.VARISTOR.asStack());
    }

    @Override
    public void render(CompoundTag data, PoseStack pose, MultiBufferSource buffer, int light) {
        CachedBuffers.partial(CEEPartialModels.VARISTOR, Blocks.ANDESITE.defaultBlockState())
                .light(light)
                .renderInto(pose, buffer.getBuffer(RenderType.cutout()));
    }

    @Override
    public boolean isValid(ItemStack stack) {
        return CEEBlocks.VARISTOR.isIn(stack);
    }

}
