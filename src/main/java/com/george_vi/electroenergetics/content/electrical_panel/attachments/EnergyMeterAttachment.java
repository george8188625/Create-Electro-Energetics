package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.cut_off_switch.SwitchingBehaviour;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.energy_meter.PanelEnergyMeterScreen;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.events.datagen.CEEAdvancements;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.UsernameCache;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class EnergyMeterAttachment extends PanelAttachment {
    public SwitchingBehaviour behaviour1;
    public SwitchingBehaviour behaviour2;
    public double activePower = 0;
    public double totalEnergy = 0;
    public int ticks = 0;
    public LerpedFloat smoothTotalEnergy = LerpedFloat.linear();
    public UUID owner;
    public boolean disconnected;
    boolean inverted = false;

    public EnergyMeterAttachment(PanelAttachmentType type) {
        super(type);
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        smoothTotalEnergy.tickChaser();
    }

    @Override
    public void render(ElectricalPanelBlockEntity be, float partialTicks, PoseStack ms,
                       MultiBufferSource buffer, int light, int overlay) {
        transformPose(ms, be);

        CachedBuffers.partial(inverted ?
                                CEEPartialModels.PANEL_ATTACHMENT_ENERGY_METER_INVERTED :
                                CEEPartialModels.PANEL_ATTACHMENT_ENERGY_METER,
                        be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.SOLID));
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        double r1 = behaviour1.resistance();
        double r2 = behaviour2.resistance();

        if (r1 < 1e+10d)
            bridges.bridge(nodes[2], nodes[0], ElectricalProperties.resistor(r1));
        if (r2 < 1e+10d)
            bridges.bridge(nodes[3], nodes[1], ElectricalProperties.resistor(r1));

        if (inverted)
            bridges.bridge(nodes[1], nodes[0], ElectricalProperties.resistor(9999));
        else
            bridges.bridge(nodes[2], nodes[3], ElectricalProperties.resistor(9999));
    }

    double[] v0s;
    double[] v1s;
    double[] v2s;

    @Override
    public void postTick(SimulationResults results) {
        v0s = results.getVoltages(nodes[2], v0s);
        v1s = results.getVoltages(nodes[3], v1s);
        v2s = results.getVoltages(nodes[0], v2s);
        double power = 0;
        double newTotalEnergy = totalEnergy;

        int length = Math.min(v0s.length, Math.min(v1s.length, v2s.length));

        for (int i = 0; i < length; i++) {

            double amps = (v0s[i] - v2s[i]) / behaviour1.resistance();

            if (Math.abs(amps) > 0.01) {
                double vs = v0s[i] - v1s[i];
                double thisPower = amps * vs;
                if (!inverted)
                    thisPower *= -1;
                newTotalEnergy += (thisPower / 72000) / (1000 * length);
                power += thisPower;
            }
        }

        power /= length;

        double v1 = results.getVoltageAt(nodes[0], nodes[2]);
        double v2 = results.getVoltageAt(nodes[1], nodes[3]);
        behaviour1.isClosed = behaviour2.isClosed = !disconnected;

        boolean loaded = level.isLoaded(pos);
        Vec3 pPos = loaded ? pos.getCenter() : null;

        behaviour1.postTickNoParticles(v1, pPos, level);
        behaviour2.postTickNoParticles(v2, pPos, level);

        if (!loaded)
            return;

        double d = (Math.abs(newTotalEnergy - totalEnergy));
        totalEnergy = newTotalEnergy;

        if ((d > 2 || ticks > 5)) {
            sendData();
            ticks = 0;
        }
        ticks++;

        activePower = disconnected ? 0 : power;
        if (owner != null && totalEnergy > 10_000) {
            Player player = Objects.requireNonNull(level.getServer()).getPlayerList().getPlayer(owner);

            if (player != null)
                CEEAdvancements.ENERGY_METER_TOTAL.awardTo(player);
        }
    }

    @Override
    public void onInserted(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        UUID owner = stack.get(CEEDataComponents.OWNER);
        Double energy = stack.get(CEEDataComponents.ENERGY);
        if (energy != null && owner != null) {
            this.owner = owner;
            this.totalEnergy = energy;
        } else {
            this.owner = player.getUUID();
        }

        inverted = hitResult.getLocation().y - pos.getY() > 0.5;
    }

    @Override
    public List<ItemStack> getDrops() {
        ItemStack stack = new ItemStack(type.item.asItem());
        stack.set(CEEDataComponents.ENERGY, totalEnergy);
        if (owner != null) {
            stack.set(CEEDataComponents.OWNER, owner);
            String username = UsernameCache.getLastKnownUsername(owner);
            stack.set(CEEDataComponents.OWNER_NAME, username == null ? "Unknown Player" : username);
        }
        return List.of(stack);
    }

    @Override
    public ItemInteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (AllItems.WRENCH.isIn(stack) || stack.getItem() instanceof WireSpoolItem || CEEItems.EMPTY_SPOOL.isIn(stack))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> this.displayScreen(player));
        return ItemInteractionResult.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    protected void displayScreen(Player player) {
        if (player instanceof LocalPlayer)
            ScreenOpener.open(new PanelEnergyMeterScreen(this));
    }


    @Override
    public void read(CompoundTag tag, boolean clientPacket) {
        totalEnergy = tag.getDouble("TotalEnergy");
        activePower = tag.getDouble("ActivePower");
        disconnected = tag.getBoolean("Disconnected");
        behaviour1 = new SwitchingBehaviour(tag.getCompound("Behaviour1"));
        behaviour2 = new SwitchingBehaviour(tag.getCompound("Behaviour2"));
        if (tag.contains("Owner"))
            owner = tag.getUUID("Owner");

        if (clientPacket)
            smoothTotalEnergy.chase(totalEnergy, 0.5, LerpedFloat.Chaser.EXP);
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket) {
        tag.putDouble("TotalEnergy", totalEnergy);
        tag.putDouble("ActivePower", activePower);
        tag.putBoolean("Disconnected", disconnected);
        tag.put("Behaviour1", behaviour1.write());
        tag.put("Behaviour2", behaviour2.write());
        if (owner != null)
            tag.putUUID("Owner", owner);
    }
}
