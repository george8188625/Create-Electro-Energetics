package com.george_vi.electroenergetics.content.fuse.fuse_held;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.RenderTypes;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class FuseHeldIndicatorBulb extends FuseHoldable {

    @Override
    public void preTick(CompoundTag data, int id1, int id2, BridgeCollector.Builder bridges, Level level, BlockPos pos) {
        bridges.resistor(id1, id2, CEEConfigs.server().resistanceValues.indicatorBulbResistance.get());
    }

    @Override
    public void postTick(CompoundTag data, int id1, int id2, SimulationResults results, Level level, BlockPos pos) {
        float light = (float) Math.min(1, Math.abs(results.getVoltageAt(pos, id1, id2) / 70));
        float lastLight = data.getFloat("Light");
        if (Math.abs(light - lastLight) > 0.01) {
            data.putFloat("Light", light);
            data.putBoolean("UpdateThisTick", true);
        }
    }

    @Override
    public NonNullList<ItemStack> getDrops(CompoundTag data) {
        return NonNullList.of(Items.AIR.getDefaultInstance(), CEEBlocks.INDICATOR_BULB.asStack());
    }

    @Override
    public boolean interact(CompoundTag data, ItemStack stack, Level level, BlockPos pos) {
        if (stack.is(CEETags.FUSE_WRENCH))
            return false;
        if (stack.getItem() instanceof DyeItem di)
            data.putString("Color", di.getDyeColor().getSerializedName());

        return true;
    }

    @Override
    public void render(CompoundTag data, PoseStack pose, MultiBufferSource buffer, int light) {
        float lightStrength = data.getFloat("Light");
        DyeColor color = DyeColor.byName(data.getString("Color"), DyeColor.WHITE);
        CachedBuffers.partial(CEEPartialModels.FUSE_HOLDER_INDICATOR_BULB, Blocks.ANDESITE.defaultBlockState())
                .light(light)
                .renderInto(pose, buffer.getBuffer(RenderType.cutout()));

        CachedBuffers.partial(CEEPartialModels.INDICATOR_BULB_TUBE, Blocks.ANDESITE.defaultBlockState())
                .color(color.getTextColor())
                .translate(-2/16f, 1.5/16f, 4/16f)
                .light(light)
                .disableDiffuse()
                .scale(0.5f, 0.75f, 0.5f)
                .renderInto(pose, buffer.getBuffer(lightStrength > 0.01f ? RenderTypes.additive() : RenderType.translucent()));


        if (lightStrength > 0.01) {
            float factor = Mth.clamp(lightStrength * 2, 0, 1);
            int newColor = ((int)(((color.getFireworkColor() >> 16) & 0xFF) * factor) << 16)
                    | ((int)(((color.getFireworkColor() >> 8) & 0xFF) * factor) << 8)
                    | ((int)((color.getFireworkColor() & 0xFF) * factor));

            CachedBuffers.partial(CEEPartialModels.INDICATOR_BULB_GLOW, Blocks.ANDESITE.defaultBlockState())
                    .color(newColor)
                    .light(0xf000f0)
                    .disableDiffuse()
                    .translate(0/16f, 7/16f, 8/16f)
                    .scale(1.25f, 2.55f, 1.25f)
                    .translate(-4/16f, -5/16f, -8/16f)
                    .renderInto(pose, buffer.getBuffer(RenderTypes.additive()));

            int cubeColor = ((int)(255 * factor) << 16)
                    | ((int)(255 * factor) << 8)
                    | ((int)(255 * factor));

            CachedBuffers.partial(CEEPartialModels.INDICATOR_BULB_CUBE, Blocks.ANDESITE.defaultBlockState())
                    .color(cubeColor)
                    .light(0xf000f0)
                    .disableDiffuse()
                    .translate(0/16f, 7/16f, 8/16f)
                    .scale(1, 2.5f, 1)
                    .translate(-4/16f, -5/16f, -8/16f)
                    .renderInto(pose, buffer.getBuffer(RenderTypes.additive()));

            int glowColor = ((int)(255 * factor * 3) << 16)
                    | ((int)(255 * factor * 3) << 8)
                    | ((int)(255 * factor * 3));

            CachedBuffers.partial(CEEPartialModels.INDICATOR_BULB_CUBE, Blocks.ANDESITE.defaultBlockState())
                    .color(glowColor)
                    .light(0xf000f0)
                    .disableDiffuse()
                    .translate(0/16f, 7/16f, 8/16f)
                    .scale(0.75f, 2.15f, 0.75f)
                    .translate(-4/16f, -5/16f, -8/16f)
                    .renderInto(pose, buffer.getBuffer(RenderTypes.additive()));
        }
    }

    @Override
    public boolean isValid(ItemStack stack) {
        return CEEBlocks.INDICATOR_BULB.isIn(stack);
    }
}
