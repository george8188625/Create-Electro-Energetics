package com.george_vi.electroenergetics.foundation.base;

import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.george_vi.simulateddevices.device.SimulatedDevice;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleElectricalDeviceBlock<T extends SimulatedDevice> extends Block implements ElectricalDeviceBlock<T>, IWrenchable {
    public SimpleElectricalDeviceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        LevelTickAccess<Block> blockTicks = level.getBlockTicks();
        if (!blockTicks.hasScheduledTick(pos, this))
            level.scheduleTick(pos, this, 1);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        List<Integer> nodes = new ArrayList<>(getNodePositions(level, pos, state).keySet());

        InfrastructureSavedData sd = InfrastructureSavedData.load(level);
        sd.registerOrUpdateNodes(pos, nodes);
        super.tick(state, level, pos, random);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // If destroyed by creative player, remove all connections, so they don't drop.
        if (level instanceof ServerLevel sl && player.isCreative()) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            for (InWorldNode node : sd.getNodesAt(pos))
                for (InWorldNodeConnection connection : sd.getConnections(node))
                    sd.removeConnection(connection);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Player player = context.getPlayer();
        // Automatically put broken wires in the inventory, or spool them if possible.
        if (context.getLevel() instanceof ServerLevel sl && player != null) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            for (InWorldNode node : sd.getNodesAt(context.getClickedPos()))
                for (InWorldNodeConnection connection : sd.getConnections(node)) {
                    WireData wireData = sd.removeConnection(connection);
                    boolean found = false;
                    for (int i = 0; i < player.getInventory().items.size(); i++) {
                        ItemStack stack = player.getInventory().items.get(i);
                        if (CEEItems.EMPTY_SPOOL.isIn(stack)) {
                            stack.shrink(1);
                            player.getInventory().placeItemBackInInventory(wireData.wireType().getSpooledItem().getDefaultInstance());
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        player.getInventory().placeItemBackInInventory(new ItemStack(wireData.wireType().getDrops(), CEEConfigs.server().wiresPerSpool.get()));
                }
        }
        return IWrenchable.super.onSneakWrenched(state, context);
    }
}
