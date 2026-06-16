package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.cut_off_switch.SwitchingBehaviour;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.events.datagen.CEEAdvancements;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class EnergyMeterAttachment extends BaseEnergyMeterAttachment {
    public SwitchingBehaviour behaviour1;
    public SwitchingBehaviour behaviour2;

    public EnergyMeterAttachment(PanelAttachmentType type) {
        super(type);
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
            bridges.bridge(nodes[2], nodes[3], ElectricalProperties.resistor(9999));
        else
            bridges.bridge(nodes[0], nodes[1], ElectricalProperties.resistor(9999));
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
    public void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if (clientPacket)
            return;
        behaviour1 = new SwitchingBehaviour(tag.getCompound("Behaviour1"));
        behaviour2 = new SwitchingBehaviour(tag.getCompound("Behaviour2"));
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if (clientPacket)
            return;
        tag.put("Behaviour1", behaviour1.write());
        tag.put("Behaviour2", behaviour2.write());
    }

    @Override
    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        if (inverted)
            return id == 2 ? Component.translatable("electroenergetics.nodes.feed") :
                    id == 0 ? Component.translatable("electroenergetics.nodes.load") :
                            Component.translatable("electroenergetics.nodes.neutral");
        return id == 0 ? Component.translatable("electroenergetics.nodes.feed") :
                id == 2 ? Component.translatable("electroenergetics.nodes.load") :
                        Component.translatable("electroenergetics.nodes.neutral");
    }
}
