package com.george_vi.electroenergetics.content.fuse.fuse_held;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.varistor.VaristorDevice;
import com.george_vi.electroenergetics.content.varistor.VaristorProperties;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class FuseHeldVaristor extends FuseHoldable {

    VaristorDevice device = new VaristorDevice(CreateElectroEnergetics.rl("varistor_fuse"), CEEConfigs.server().voltageValues.varistorVoltage::get);
    VaristorDevice.DataHolder dataholder = new VaristorDevice.DataHolder();
    VaristorProperties properties = new VaristorProperties(dataholder);

    //Todo could we hold a VaristorDevice as a Variable?
    //todo Problem: we never write the current voltage into the actual DataHolder, which tickVaristor reads from

    @Override
    public void preTick(CompoundTag data, int id1, int id2, BridgeCollector.Builder builder, Level level, BlockPos pos) {
        if (!data.contains("VoltageAtVaristor")) {
            data.putDouble("VoltageAtVaristor", 1_000_000);
        }
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
         /*var readData = device.read(data);
        if (readData instanceof VaristorDevice.DataHolder) {
            device.postTick(pos, level, results, readData);
        }
            else {
            data = device.write(new VaristorDevice.DataHolder());
        }*/


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
