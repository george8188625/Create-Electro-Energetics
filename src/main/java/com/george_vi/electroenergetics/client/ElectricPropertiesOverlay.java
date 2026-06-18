package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.mojang.blaze3d.platform.Window;
import com.simibubi.create.AllKeys;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricPropertiesOverlay implements LayeredDraw.Layer {
    public static final ElectricPropertiesOverlay INSTANCE = new ElectricPropertiesOverlay();

    NodeVoltageHolder.VoltageEntry voltage;
    InWorldNode node;
    float amperage;
    OverlayMode mode;
    public boolean invalidConnection;
    public boolean connectionTooLong;
    public int ticks = 0;

    public void setHoveredNode(NodeVoltageHolder.VoltageEntry voltage, InWorldNode node) {
        this.voltage = voltage;
        this.node = node;
        if (mode != OverlayMode.HOVER_NODE) {
            mode = OverlayMode.HOVER_NODE;
            ticks = 0;
        }
    }

    public void setAmmeter(float amperage) {
        this.amperage = amperage;
        if (mode != OverlayMode.AMMETER) {
            mode = OverlayMode.AMMETER;
            ticks = 0;
        }
    }

    public void setAmperageSetting(int currentAmperage) {
        amperage = currentAmperage;
        if (mode != OverlayMode.AMPERAGE_CONFIGURATION) {
            mode = OverlayMode.AMPERAGE_CONFIGURATION;
            ticks = 0;
        }
    }

    public void removeHoveredNode() {
        if (mode == OverlayMode.HOVER_NODE) {
            mode = OverlayMode.NONE;
            ticks = 0;
        }
    }

    public void removeMeter() {
        if (mode == OverlayMode.AMMETER) {
            mode = OverlayMode.NONE;
            ticks = 0;
        }
    }

    public void removeAmperageSetting() {
        if (mode == OverlayMode.AMPERAGE_CONFIGURATION) {
            mode = OverlayMode.NONE;
            ticks = 0;
        }
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui)
            return;

        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() / 2 + 33;
        float alpha = 1;

        Color color = new Color(0xffffff);
        Color titleColor = (invalidConnection || connectionTooLong) ? new Color(ChatFormatting.RED.getColor()) : new Color(0xFBDC7D);
        color.setAlpha(alpha);
        titleColor.setAlpha(alpha);
        if (mode == OverlayMode.HOVER_NODE) {
            if (node == null)
                return;
            BlockState state = mc.level.getBlockState(node.sourcePos());
            MutableComponent baseNodeLabel;

            if ((state.getBlock() instanceof ElectricalDeviceBlock<?> db))
                baseNodeLabel = db.getNodeLabel(mc.level, node.sourcePos(), state, node.id());
            else
                baseNodeLabel = CEELang.nodeLabel("node");

            String customNodeLabel = WireRenderer.getNodeLabel(node);
            MutableComponent nodeLabel = customNodeLabel == null ?
                    baseNodeLabel.withStyle(ChatFormatting.BOLD):
                    Component.literal(customNodeLabel).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.BOLD);
            if (CEEConfigs.client().debugNodeID.get())
                nodeLabel.append(" [" + node.id() + "]");
            graphics.drawString(mc.font, nodeLabel, x - mc.font.width(nodeLabel) / 2, y, titleColor.getRGB());

            y += 12;
            MutableComponent formattedVoltage = CEELang.formatVoltage(voltage.rmsVoltage).component();
            graphics.drawString(mc.font, formattedVoltage, x - mc.font.width(formattedVoltage) / 2, y, color.getRGB());

            if (invalidConnection || connectionTooLong) {
                MutableComponent component = CEELang.builder()
                        .translate(connectionTooLong ? "wire_spool.too_far_away" : "wire_spool.invalid_connection")
                        .style(ChatFormatting.RED)
                        .component();
                graphics.drawString(mc.font, component, x - mc.font.width(component) / 2, y + 12, 0);
            }

        } else if (mode == OverlayMode.AMMETER) {
            MutableComponent nodeLabel = CEELang.builder()
                    .translate("clamp_meter.current")
                    .component().withStyle(ChatFormatting.BOLD);
            graphics.drawString(mc.font, nodeLabel, x - mc.font.width(nodeLabel) / 2, y, titleColor.getRGB());
            y += 12;
            MutableComponent formattedAmperage = CEELang.formatAmperage(amperage).component();
            graphics.drawString(mc.font, formattedAmperage, x - mc.font.width(formattedAmperage) / 2, y, color.getRGB());
        } else if (mode == OverlayMode.AMPERAGE_CONFIGURATION) {

            Window window = mc.getWindow();
            y = window.getGuiScaledHeight() - 61 - 12;
            MutableComponent nodeLabel = CEELang.builder()
                    .translate("fuse.set_amperage")
                    .space()
                    .add(CEELang.formatAmperage(amperage).color(0xFFFFFF))
                    .component();
            graphics.drawString(mc.font, nodeLabel, x - mc.font.width(nodeLabel) / 2, y, titleColor.getRGB());

            if (AllKeys.altDown()) {
                ticks = 0;
                return;
            }

            y += 12;

            MutableComponent text = CEELang.translateDirect("fuse.hold_to_configure");

            graphics.drawString(mc.font, text, x - mc.font.width(text) / 2, y, color.setAlpha(Mth.clamp(ticks - 10, 1, 10) / 10f).getRGB(), false);
        }
    }

    enum OverlayMode {
        NONE, HOVER_NODE, AMMETER, AMPERAGE_CONFIGURATION;
    }
}
