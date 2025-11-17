package com.george_vi.electroenergetics.foundation;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.DeviceBlock;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricPropertiesOverlay implements LayeredDraw.Layer {
    public static final ElectricPropertiesOverlay INSTANCE = new ElectricPropertiesOverlay();
    
    double voltage;
    InWorldNode node;
    float amperage;
    OverlayMode mode;
    int directionPointerAngle;

    public void setHoveredNode(Double voltage, InWorldNode node) {
        this.voltage = voltage;
        this.node = node;
        this.mode = OverlayMode.HOVER_NODE;
    }

    public void setAmmeter(float amperage, int direction) {
        this.amperage = amperage;
        this.mode = OverlayMode.AMMETER;
        this.directionPointerAngle = direction;
    }

    public void setVoltMeter(float voltage) {
        this.voltage = voltage;
        this.mode = OverlayMode.VOLTMETER;
    }

    public void removeHoveredNode() {
        if (mode == OverlayMode.HOVER_NODE)
            mode = OverlayMode.NONE;
    }

    public void removeMeter() {
        if (mode == OverlayMode.AMMETER ||
                mode == OverlayMode.VOLTMETER)
            mode = OverlayMode.NONE;
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
        Color titleColor = new Color(0xFBDC7D);
        color.setAlpha(alpha);
        titleColor.setAlpha(alpha);
        if (mode == OverlayMode.HOVER_NODE) {
            if (node == null)
                return;
            BlockState state = mc.level.getBlockState(node.sourcePos());
            if (!(state.getBlock() instanceof DeviceBlock db))
                return;

            MutableComponent nodeLabel = db.getNodeLabel(mc.level, node.sourcePos(), state, node.id()).withStyle(ChatFormatting.BOLD);
            graphics.drawString(mc.font, nodeLabel, x - mc.font.width(nodeLabel) / 2, y, titleColor.getRGB());

            y += 12;
            MutableComponent formattedVoltage = CEELang.formatVoltage(voltage).component();
            graphics.drawString(mc.font, formattedVoltage, x - mc.font.width(formattedVoltage) / 2, y, color.getRGB());
        }
        if (mode == OverlayMode.AMMETER) {
            // ⇖ ⇙ ⇘ ⇗ ← → ↓ ↑

//            MutableComponent directionLabel = Component.literal("⇖ ⇙ ⇘ ⇗ ← → ↓ ↑").withStyle(ChatFormatting.BOLD);
//            graphics.drawString(mc.font, directionLabel, x - mc.font.width(directionLabel) / 2, y, color.getRGB());
//            y += 12;

            MutableComponent formattedAmperage = CEELang.formatAmperage(amperage).component();
            graphics.drawString(mc.font, formattedAmperage, x - mc.font.width(formattedAmperage) / 2, y, color.getRGB());
        }
    }

    enum OverlayMode {
        NONE, HOVER_NODE, AMMETER, VOLTMETER;
    }
}
