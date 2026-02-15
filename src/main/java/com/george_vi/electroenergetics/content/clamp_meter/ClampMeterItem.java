package com.george_vi.electroenergetics.content.clamp_meter;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.client.NodeVoltageHolder;
import com.george_vi.electroenergetics.content.wire.interaction.InteractWirePacket;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionHandler;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.client.ElectricPropertiesOverlay;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnectionPoint;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

            if (!point.connection().equals(stack.getOrDefault(CEEDataComponents.NODE_CONNECTION, point.connection()))) {
                CatnipServices.NETWORK.sendToServer(new InteractWirePacket(point));
                return;
            }

            double voltage = NodeVoltageHolder.getVoltageBetween(point.node1(), point.node2());
            Pair<InWorldNodeConnection, WireData> wire = null;
            for (Pair<InWorldNodeConnection, WireData> connection : WireRenderer.getAllConnections()) {
                if (connection.getFirst().equals(new InWorldNodeConnection(point.node1(), point.node2()))) {
                    wire = connection;
                    break;
                }
            }

            if (wire == null)
                return;

            double resistance = SimulationTicker.getWireResistance(point.node1(), point.node2(), wire.getSecond().wireType().getResistance());
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> setMetering((float) (voltage / resistance)));
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        stack.remove(CEEDataComponents.NODE_CONNECTION);
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
        ElectricPropertiesOverlay.INSTANCE.setAmmeter(Math.abs(amperage), 0);
        NodeConnectionPoint point = WireInteractionHandler.targetedPoint;
        if (point == null || Math.abs(amperage) < 1e-2d)
            return;
        Level level = Minecraft.getInstance().level;
        WireData wireData = WireRenderer.getConnectionData(new InWorldNodeConnection(point.node1(), point.node2()));

        Vec3 pos1 = point.node1().getPosition(level);
        Vec3 pos2 = point.node2().getPosition(level);
        double distance = pos1.distanceTo(pos2);
        if (distance == 0)
            return;
        for (int i = 0; i < 4; i++) {
            int ticks = AnimationTickHolder.getTicks();
            int page = (int) Math.floor((ticks + 6 * i) / 24d);
            int dotID = page << 2 + i;

            float progress = ((ticks + 6 * i) % 24) / 24f;

            float pointOnWire = (float) (((point.point() * distance) + ((amperage > 0 ? progress : 1.0 - progress) * 1.2f) - 0.6f) / distance);
            Vec3 position = QuadraticWireHelper.posAt(pos1, pos2, pointOnWire > 1 ? 1 : pointOnWire < 0 ? 0 : pointOnWire, wireData.wireType().getSag());
            Outliner.getInstance().chaseAABB("cee_clamp_meter_current_visualization_" + dotID, AABB.ofSize(position, 0.01, 0.01, 0.01))
                    .lineWidth(0.15f * Math.min(1, progress * 4))
                    .colored(Color.SPRING_GREEN)
                    .disableLineNormals();
        }
    }
}
