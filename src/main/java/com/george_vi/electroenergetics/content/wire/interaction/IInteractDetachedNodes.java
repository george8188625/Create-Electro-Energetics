package com.george_vi.electroenergetics.content.wire.interaction;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IInteractDetachedNodes {
    void interactDetachedNode(InWorldNode node, Player player, Level level, ItemStack stack);
}
