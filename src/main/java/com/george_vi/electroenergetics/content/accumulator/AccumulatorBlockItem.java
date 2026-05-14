package com.george_vi.electroenergetics.content.accumulator;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class AccumulatorBlockItem extends BlockItem {
    public AccumulatorBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        BlockState state = level.getBlockState(pos);
        if (!CEEBlocks.ACCUMULATOR.has(state) ||
                state.getValue(AccumulatorBlock.STACK).isDouble())
            return super.useOn(context);

        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Direction facing = state.getValue(AccumulatorBlock.FACING);
        boolean roll = state.getValue(AccumulatorBlock.ROLL);
        boolean flip = state.getValue(AccumulatorBlock.FLIP);

        boolean clickedOnRight = AccumulatorBlock.clickedOnFirst(roll, facing,
                context.getClickLocation().subtract(Vec3.atLowerCornerOf(pos)));

        boolean clickedOnUpper = AccumulatorBlock.clickedOnFirst(!roll, facing,
                context.getClickLocation().subtract(Vec3.atLowerCornerOf(pos)));

        boolean newOpposite;
        boolean newFlip = flip;

        if (clickedOnUpper == flip) {
            newOpposite = false;
        } else {
            newOpposite = true;
            newFlip = clickedOnRight ^ flip ^ !roll;
        }

        BlockState newState = state.setValue(AccumulatorBlock.STACK, newOpposite ?
                AccumulatorStack.DOUBLE_OPPOSITE :
                AccumulatorStack.DOUBLE_PARALLEL)
                .setValue(AccumulatorBlock.FLIP, newFlip);

        level.setBlockAndUpdate(pos, newState);
        SoundType soundtype = newState.getSoundType(level, pos, player);
        level.playSound(
                player,
                pos,
                soundtype.getPlaceSound(),
                SoundSource.BLOCKS,
                (soundtype.getVolume() + 1.0F) / 2.0F,
                soundtype.getPitch() * 0.8F
        );
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, newState));
        stack.consume(1, player);

        if (level instanceof ServerLevel sl) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            AccumulatorDevice device = sd.deviceSD.getDevice(pos, AccumulatorDevice.class);
            if (device != null)
                device.cell2Charge = 0;
            if (newOpposite) {
                if (clickedOnRight ^ roll) {
                    sd.migrateConnections(new InWorldNode(0, pos), new InWorldNode(2, pos));
                    sd.migrateConnections(new InWorldNode(1, pos), new InWorldNode(3, pos));
                    if (device != null) {
                        device.cell2Charge = device.cell1Charge;
                        device.cell1Charge = 0;
                    }
                }
            } else {
                if (!clickedOnRight ^ !roll) {
                    sd.migrateConnections(new InWorldNode(0, pos), new InWorldNode(2, pos));
                    sd.migrateConnections(new InWorldNode(1, pos), new InWorldNode(3, pos));
                    if (device != null) {
                        device.cell2Charge = device.cell1Charge;
                        device.cell1Charge = 0;
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
