package com.george_vi.electroenergetics.content.fuse.fuse_held;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

public abstract class FuseHoldable {
    public static Map<ResourceLocation, FuseHoldable> ALL = new HashMap<>();

    public static FuseHoldable register(FuseHoldable fuseHoldable, ResourceLocation location) {
        ALL.put(location, fuseHoldable);
        return fuseHoldable;
    }

    public abstract void preTick(CompoundTag data, int id1, int id2, BridgeCollector.Builder bridges, Level level, BlockPos pos);

    public void postTick(CompoundTag data, int id1, int id2, SimulationResults results, Level level, BlockPos pos) {};

    public abstract NonNullList<ItemStack> getDrops(CompoundTag data);

    @OnlyIn(Dist.CLIENT)
    public abstract void render(CompoundTag data, PoseStack pose, MultiBufferSource buffer, int light);

    public ResourceLocation getID() {
        for (Map.Entry<ResourceLocation, FuseHoldable> e : ALL.entrySet()) {
            if (e.getValue() == this)
                return e.getKey();
        }
        return CreateElecrtoEnergetics.rl("empty");
    }

    public abstract boolean isValid(ItemStack stack);

    public void onPlace(CompoundTag data, ItemStack stack, Level level, BlockPos pos) {};


    /**
     * When the player right-clicks it.
     * @return false if the fuse is supposed to be taken off.
     */
    public boolean interact(CompoundTag data, ItemStack stack, Level level, BlockPos pos) {
        return false;
    }

    public static class CopperConductor extends FuseHoldable {

        @Override
        public void preTick(CompoundTag data, int id1, int id2, BridgeCollector.Builder bridges, Level level, BlockPos pos) {
            bridges.resistor(id1, id2, 0.1);
        }

        @Override
        public NonNullList<ItemStack> getDrops(CompoundTag data) {
            return NonNullList.of(Items.AIR.getDefaultInstance(), Items.COPPER_INGOT.getDefaultInstance());
        }

        @Override
        public void render(CompoundTag data, PoseStack pose, MultiBufferSource buffer, int light) {
            CachedBuffers.partial(CEEPartialModels.FUSE_HOLDER_COPPER_CONDUCTOR, Blocks.ANDESITE.defaultBlockState())
                    .light(light)
                    .renderInto(pose, buffer.getBuffer(RenderType.cutout()));
        }

        @Override
        public boolean isValid(ItemStack stack) {
            return stack.is(Items.COPPER_INGOT);
        }
    }

}