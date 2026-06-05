package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.WireType;
import com.george_vi.electroenergetics.simulation.infrastructure.InWorldNodeData;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.george_vi.electroenergetics.simulation.infrastructure.detached_nodes.DetachedNodeType;
import com.simibubi.create.AllSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class HangingWireSpoolItem extends Item {
    final Supplier<WireType> wireType;

    public HangingWireSpoolItem(Properties properties, Supplier<WireType> wireType) {
        super(properties);
        this.wireType = wireType;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(CEEDataComponents.SELECTED_NODE);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack heldItem = context.getItemInHand();
        BlockState state = level.getBlockState(pos);

        InWorldNode hoveredNode = InWorldNode.closestNode(level, pos, state, 1.5f, context.getClickLocation());

        if (hoveredNode == null)
            return InteractionResult.PASS;

        if (!(level instanceof ServerLevel sl))
            return InteractionResult.SUCCESS;

        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);

        BlockPos hoveredPos = hoveredNode.sourcePos();

        BlockState hoveredState = level.getBlockState(hoveredPos);

        if (hoveredState.getBlock() instanceof ElectricalDeviceBlock<?> edb)
            edb.ensureNodesExist(sl, hoveredPos, hoveredState);

        if (!sd.getNodes().contains(hoveredNode))
            return InteractionResult.FAIL;

        Vec3 newNodePos = context.getClickLocation();

        InWorldNodeData newNodeData = sd.createDetachedNode(DetachedNodeType.PHYSICS_UNINITIALIZED, newNodePos);
        InWorldNode newNode = newNodeData.node;

        if (wireType.get() instanceof WireType.Dyeable dyeableWire &&
                player != null &&
                player.getOffhandItem().getItem() instanceof DyeItem di) {

            WireType newWiretype = dyeableWire.getDyed(di.getDyeColor());
            sd.connect(hoveredNode, newNode, WireData.ofLength(newWiretype, 4));
        } else
            sd.connect(hoveredNode, newNode, WireData.ofLength(wireType.get(), 4));

        AllSoundEvents.WRENCH_REMOVE.playOnServer(level, pos);

        if (player != null && !player.isCreative()) {
            heldItem.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

}
