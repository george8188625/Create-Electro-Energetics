package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEEWireAttachments;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.WireTargeting;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.foundation.NodeConnectionPoint;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.WireData;
import net.createmod.catnip.data.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(net.minecraft.world.item.HangingSignItem.class)
public class HangingSignItemMixin extends SignItem implements WireTargeting {

    public HangingSignItemMixin(Properties properties, Block standingBlock, Block wallBlock) {
        super(properties, standingBlock, wallBlock);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (tryUseOnWire(level, player, usedHand) == InteractionResult.SUCCESS)
            return InteractionResultHolder.success(player.getItemInHand(usedHand));
        return super.use(level, player, usedHand);
    }

    @Override
    public void interactWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        if (!(level instanceof ServerLevel sl))
            return;
        if (point.node1().compareTo(point.node2()) > 0)
            point = point.reverse();

        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        WireData data = sd.getConnectionData(new NodeConnection(point.node1(), point.node2()));
        for (Pair<Float, WireAttachment> attachment : data.attachments()) {
            if (Math.abs(point.point() - attachment.getFirst()) > attachment.getSecond().getWidth() / 2 + 0.5)
                return;
        }

        List<Pair<Float, WireAttachment>> attachments = new ArrayList<>(data.attachments());
        attachments.add(Pair.of(point.point(), new WireAttachment(CEEWireAttachments.OAK_HANGING_SIGN.get())));
        sd.setConnectionData(new NodeConnection(point.node1(), point.node2()), new WireData(data.wireType(), data.temperature(), attachments));

        if (!player.isCreative())
            stack.setCount(1);
    }
}
