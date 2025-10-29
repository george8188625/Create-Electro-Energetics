package com.george_vi.electroenergetics.content.indicator_bulb;

import com.george_vi.electroenergetics.CEEBlocks;
import net.minecraft.core.BlockPos;
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

public class IndicatorBulbBlockItem extends BlockItem {
    public IndicatorBulbBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        BlockState state = level.getBlockState(pos);
        if (!CEEBlocks.INDICATOR_BULB.has(state))
            return super.useOn(context);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        int currentSide = state.getValue(IndicatorBulbBlock.SIDE);
        boolean clickedOnFirst = IndicatorBulbBlock.clickedOnFirst(state, context.getClickLocation().subtract(Vec3.atLowerCornerOf(pos)));
        boolean clickedOnEmpty = clickedOnFirst ? currentSide == 1 : currentSide == 0;
        if (clickedOnEmpty) {
            BlockState newState = state.setValue(IndicatorBulbBlock.SIDE, 2);
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
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
}
