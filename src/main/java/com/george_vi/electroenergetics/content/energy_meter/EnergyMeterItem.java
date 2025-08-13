package com.george_vi.electroenergetics.content.energy_meter;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.foundation.CEELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.UsernameCache;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class EnergyMeterItem extends BlockItem {
    public EnergyMeterItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        boolean updated = false;
        UUID owner = stack.get(CEEDataComponents.OWNER);
        Double energy = stack.get(CEEDataComponents.ENERGY);
        if (energy != null && owner != null) {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof EnergyMeterBlockEntity be) {
                be.owner = owner;
                be.totalEnergy = energy.floatValue();
                updated = true;
            }
        }
        return updated || super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        String owner = stack.get(CEEDataComponents.OWNER_NAME);
        double energy = stack.getOrDefault(CEEDataComponents.ENERGY, 0d);
        if (owner != null) {
            CEELang.builder().translate("tooltip.energy_meter.energy", CEELang.formatEnergy(energy * 1000))
                    .style(ChatFormatting.GRAY).addTo(tooltipComponents);

            CEELang.builder().translate("tooltip.energy_meter.owner", owner)
                    .style(ChatFormatting.GRAY).addTo(tooltipComponents);
        }
    }
}
