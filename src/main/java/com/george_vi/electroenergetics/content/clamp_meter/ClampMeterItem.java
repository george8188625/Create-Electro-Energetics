package com.george_vi.electroenergetics.content.clamp_meter;

import com.george_vi.electroenergetics.content.wire.interaction.InteractWirePacket;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionHandler;
import com.george_vi.electroenergetics.content.wire.WireRenderer;
import com.george_vi.electroenergetics.foundation.ElectricPropertiesOverlay;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.foundation.NodeConnectionPoint;
import com.george_vi.electroenergetics.simulation.WireData;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ClampMeterItem extends Item {

    public ClampMeterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {

        if (level.isClientSide()) {
            NodeConnectionPoint point = WireInteractionHandler.targetedPoint;
            if (point == null || usedHand != InteractionHand.MAIN_HAND)
                return InteractionResultHolder.pass(player.getItemInHand(usedHand));
            CatnipServices.NETWORK.sendToServer(new InteractWirePacket(point));
            player.startUsingItem(usedHand);
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 9999;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player))
            return;
        if (level.isClientSide) {
            NodeConnectionPoint point = WireInteractionHandler.targetedPoint;
            if (point == null) {
                player.stopUsingItem();
                return;
            }

            Float v1 = WireRenderer.getAllVoltages().get(point.node1());
            Float v2 = WireRenderer.getAllVoltages().get(point.node2());
            Pair<NodeConnection, WireData> wire = null;
            for (Pair<NodeConnection, WireData> connection : WireRenderer.getAllConnections()) {
                if (connection.getFirst().equals(new NodeConnection(point.node1(), point.node2()))) {
                    wire = connection;
                    break;
                }
            }

            if (v1 == null || v2 == null || wire == null)
                return;

            double resistance = SimulationTicker.getWireResistance(point.node1(), point.node2(), wire.getSecond().wireType());
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> setMetering((float) (Math.abs(v1 - v2) / resistance)));
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        CatnipServices.PLATFORM.executeOnClientOnly(() -> this::stopMetering);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        onStopUsing(stack, livingEntity, timeCharged);
    }

    @OnlyIn(Dist.CLIENT)
    protected void stopMetering() {
        ElectricPropertiesOverlay.INSTANCE.removeMeter();
    }

    @OnlyIn(Dist.CLIENT)
    protected void setMetering(float amperage) {
        ElectricPropertiesOverlay.INSTANCE.setAmmeter(amperage, 0);
    }
}
