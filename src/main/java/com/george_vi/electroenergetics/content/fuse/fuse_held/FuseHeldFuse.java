package com.george_vi.electroenergetics.content.fuse.fuse_held;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.content.cut_off_switch.SwitchingBehaviour;
import com.george_vi.electroenergetics.content.fuse.FuseHolderBlock;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.foundation.device.ElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FuseHeldFuse extends FuseHoldable {

    @Override
    public void preTick(CompoundTag data, int id1, int id2, BridgeCollector.Builder bridges, Level level, BlockPos pos) {
        SwitchingBehaviour behaviour = new SwitchingBehaviour(data.getCompound("Behaviour"));
        double r = behaviour.resistance();
        if (r < 1e+10d)
            bridges.resistor(id1, id2, r);
    }

    @Override
    public void postTick(CompoundTag data, int id1, int id2, SimulationResults results, Level level, BlockPos pos) {
        SwitchingBehaviour behaviour = new SwitchingBehaviour(data.getCompound("Behaviour"));
        boolean isBroken = data.getBoolean("Broken");
        float setAmperage = (float) data.getDouble("SetAmperage");

        double voltage = Math.abs(results.getVoltageAt(pos, id1, id2));
        double current = voltage / behaviour.resistance();

        float temp = data.getFloat("Temp");
        temp = ElectricalDevice.updateTemp(temp, (float) current);

        behaviour.isClosed = !isBroken;
        behaviour.postTick(voltage / 2, pos.getCenter(), level);

        if (current < 1 || isBroken)
            temp = 0;
        data.putFloat("Temp", temp);

        if (temp > ElectricalDevice.finalTempAt(setAmperage)) {
            if (!isBroken) {
                if (level.isLoaded(pos)) {
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof FuseHolderBlock block) {
                        Vec3 sparkPos = block.getNodePosition(level, pos, state, id1).add(block.getNodePosition(level, pos, state, id2)).scale(0.5).add(Vec3.atLowerCornerOf(pos));
                        sparkPos = sparkPos.relative(state.getValue(DirectionalRolledDeviceBlock.FACING), 0.25);
                        CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, sparkPos, 20, new SendSparkPacket(sparkPos, SendSparkPacket.SparkSize.SMALL));
                    }
                }

                data.putBoolean("UpdateThisTick", true);
            }

            data.putBoolean("Broken", true);

        }
        data.put("Behaviour", behaviour.write());
    }

    @Override
    public NonNullList<ItemStack> getDrops(CompoundTag data) {
        ItemStack stack = data.getBoolean("Broken") ? CEEBlocks.BROKEN_FUSE.asStack() : CEEBlocks.FUSE.asStack();
        int setAmperage = (int) data.getDouble("SetAmperage");
        if (setAmperage != 100)
            stack.set(CEEDataComponents.FUSE_AMPERAGE, setAmperage);
        return NonNullList.of(Items.AIR.getDefaultInstance(), stack);
    }

    @Override
    public void render(CompoundTag data, PoseStack pose, MultiBufferSource buffer, int light) {
        CachedBuffers.partial(data.getBoolean("Broken") ? CEEPartialModels.FUSE_HOLDER_BROKEN_FUSE : CEEPartialModels.FUSE_HOLDER_FUSE, Blocks.ANDESITE.defaultBlockState())
                .light(light)
                .renderInto(pose, buffer.getBuffer(RenderType.cutout()));
    }

    @Override
    public boolean isValid(ItemStack stack) {
        return CEEBlocks.FUSE.isIn(stack) || CEEBlocks.BROKEN_FUSE.isIn(stack);
    }

    @Override
    public void onPlace(CompoundTag data, ItemStack stack, Level level, BlockPos pos) {
        if (CEEBlocks.BROKEN_FUSE.isIn(stack))
            data.putBoolean("Broken", true);
        int currentAmperage = stack.getOrDefault(CEEDataComponents.FUSE_AMPERAGE, 100);

        data.putDouble("SetAmperage", currentAmperage);
    }
}
