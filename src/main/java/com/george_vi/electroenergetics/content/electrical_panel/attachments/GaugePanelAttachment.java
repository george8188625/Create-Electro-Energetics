package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelLayoutType;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.RMSHolder;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public class GaugePanelAttachment extends PanelAttachment {
    public static final double AMMETER_RESISTANCE = 0.01;
    public float dialTarget;
    public float dialState;
    public float prevDialState;
    public final boolean voltmeter;
    public RMSHolder rmsVoltages;
    public double value;

    private GaugePanelAttachment(PanelAttachmentType type, boolean voltmeter) {
        super(type);
        this.voltmeter = voltmeter;
    }

    public static PanelAttachment ammeter(PanelAttachmentType type) {
        return new GaugePanelAttachment(type, false);
    }

    public static PanelAttachment voltmeter(PanelAttachmentType type) {
        return new GaugePanelAttachment(type, true);
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {

        prevDialState = dialState;
        dialState += (dialTarget - dialState) * .125f;
        if (dialState > 1 && level.random.nextFloat() < 1 / 2f)
            dialState -= (dialState - 1) * level.random.nextFloat();
    }

    @Override
    public void render(ElectricalPanelBlockEntity be, float partialTicks, PoseStack ms,
                       MultiBufferSource buffer, int light, int overlay) {
        transformPose(ms, be);
        float progress = Mth.lerp(partialTicks, prevDialState, dialState);

        if (miniature()) {
            CachedBuffers.partial(voltmeter ?
                                    CEEPartialModels.PANEL_ATTACHMENT_SMOL_VOLTMETER :
                                    CEEPartialModels.PANEL_ATTACHMENT_SMOL_AMMETER,
                            be.getBlockState())
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            CachedBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_SMOL_DIAL, be.getBlockState())
                    .translate(0, progress * 0.35f, 0)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
        } else {
            CachedBuffers.partial(voltmeter ?
                                    CEEPartialModels.PANEL_ATTACHMENT_VOLTMETER :
                                    CEEPartialModels.PANEL_ATTACHMENT_AMMETER,
                            be.getBlockState())
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            CachedBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_DIAL, be.getBlockState())
                    .translate(3.5 / 16f, 6.5 / 16f, 8.75 / 16f)
                    .rotateZ(Mth.PI / 2 * progress)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        }
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        bridges.bridge(nodes[0], nodes[1], ElectricalProperties.resistor(voltmeter ? 1_000_000 : AMMETER_RESISTANCE));
    }

    @Override
    public void postTick(SimulationResults results) {
        if (!level.isLoaded(pos))
            return;

        double vd = results.getVoltageAt(nodes[0], nodes[1]);
        this.rmsVoltages.add(vd);
        vd = this.rmsVoltages.get();
        value = voltmeter ? Math.abs(vd) : Math.abs(vd) / AMMETER_RESISTANCE;
        if (level.getBlockEntity(pos) instanceof ElectricalPanelBlockEntity be)
            be.sendData();
    }

    @Override
    public boolean addToGoggleTooltip(ElectricalPanelBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {
        if (label != null)
            CEELang.builder()
                    .text(label)
                    .forGoggles(tooltip);
        else
            CreateLang.translate("gui.gauge.info_header")
                    .forGoggles(tooltip);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate(voltmeter ? "generic.voltage" : "generic.current")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        double v = Math.abs(value);
        if (v  > 1)
            v = Math.round(v);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(TooltipHelper.makeProgressBar(3, dialState < 0.01 ? 0 : dialState < 0.33 ? 1 : dialState < 0.66 ? 2 : 3))
                .space()
                .add(CreateLang.number(v))
                .add(Component.translatable(voltmeter ? "electroenergetics.generic.volts" : "electroenergetics.generic.amps"))
                .style(dialState < 0.01 ? ChatFormatting.DARK_GRAY :
                        dialState < 0.33f ? ChatFormatting.GREEN :
                        dialState < 0.66f ? (voltmeter ? ChatFormatting.AQUA : ChatFormatting.GOLD) :
                        (voltmeter ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.RED))
                .forGoggles(tooltip);
        return true;
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket) {
        value = tag.getDouble("Value");
        if (clientPacket) {
            dialTarget = (float) Mth.clamp(voltmeter ? value / 1000 : value / 40, 0, 1);
            return;
        }

        this.rmsVoltages = new RMSHolder(2);
        this.rmsVoltages.read(tag, "Voltages");
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket) {
        tag.putDouble("Value", value);
        if (clientPacket)
            return;

        this.rmsVoltages.write(tag, "Voltages");
    }

    private boolean miniature() {
        return slot.layoutType() == ElectricalPanelLayoutType.THIRD;
    }
}
