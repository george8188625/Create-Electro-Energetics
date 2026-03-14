package com.george_vi.electroenergetics.content.fuse.fuse_held;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.content.cut_off_switch.SwitchingBehaviour;
import com.george_vi.electroenergetics.content.fuse.FuseHolderBlock;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FuseHeldSwitch extends FuseHoldable {

    @Override
    public void preTick(CompoundTag data, int id1, int id2, BridgeCollector.Builder bridges, Level level, BlockPos pos) {
        SwitchingBehaviour behaviour = new SwitchingBehaviour(data.getCompound("Behaviour"));
        double resistance = behaviour.resistance();
        if (resistance != 0)
            bridges.resistor(id1, id2, resistance);
    }

    @Override
    public void postTick(CompoundTag data, int id1, int id2, SimulationResults results, Level level, BlockPos pos) {
        Vec3 sparkPos = null;
        if (level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof FuseHolderBlock block) {
                sparkPos = block.getNodePosition(level, pos, state, id1)
                        .add(block.getNodePosition(level, pos, state, id2))
                        .scale(0.5).add(Vec3.atLowerCornerOf(pos));
                sparkPos = sparkPos.relative(state.getValue(DirectionalRolledDeviceBlock.FACING), 0.25);
            }
        }

        SwitchingBehaviour behaviour = new SwitchingBehaviour(data.getCompound("Behaviour"));
        behaviour.isClosed = data.getBoolean("Closed");
        behaviour.postTick(results.getVoltageAt(pos, id1, id2), sparkPos, level);
        data.put("Behaviour", behaviour.write());
    }

    @Override
    public NonNullList<ItemStack> getDrops(CompoundTag data) {
        return NonNullList.of(Items.AIR.getDefaultInstance(), CEEBlocks.CUT_OFF_SWITCH.asStack());
    }

    @Override
    public boolean interact(CompoundTag data, ItemStack stack, Level level, BlockPos pos) {
        if (stack.is(CEETags.FUSE_WRENCH))
            return false;
        data.putBoolean("Closed", !data.getBoolean("Closed"));
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.1f, 1);

        return true;
    }

    @Override
    public void render(CompoundTag data, PoseStack pose, MultiBufferSource buffer, int light) {
        CachedBuffers.partial(data.getBoolean("Closed") ? CEEPartialModels.FUSE_HOLDER_SWITCH_CLOSED :
                        CEEPartialModels.FUSE_HOLDER_SWITCH, Blocks.ANDESITE.defaultBlockState())
                .light(light)
                .renderInto(pose, buffer.getBuffer(RenderType.solid()));
    }

    @Override
    public boolean isValid(ItemStack stack) {
        return CEEBlocks.CUT_OFF_SWITCH.isIn(stack);
    }
}
