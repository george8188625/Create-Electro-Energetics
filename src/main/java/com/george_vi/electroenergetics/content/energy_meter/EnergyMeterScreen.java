package com.george_vi.electroenergetics.content.energy_meter;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEGuiTextures;
import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class EnergyMeterScreen extends AbstractSimiScreen {

    IconButton confirmButton;
    int disconnectX;
    int disconnectY;
    int resetX;
    int resetY;


    EnergyMeterBlockEntity be;
    public EnergyMeterScreen(EnergyMeterBlockEntity be) {
        super(Component.translatable("electroenergetics.gui.energy_meter"));
        this.be = be;
    }

    @Override
    protected void init() {
        setWindowSize(CEEGuiTextures.ENERGY_METER.getWidth(), CEEGuiTextures.ENERGY_METER.getHeight());
        setWindowOffset(-20, 0);

        super.init();

        confirmButton = new IconButton(guiLeft + CEEGuiTextures.ENERGY_METER.getWidth() - 33, guiTop + CEEGuiTextures.ENERGY_METER.getHeight() - 25, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> onClose());
        addRenderableWidget(confirmButton);

        disconnectX = guiLeft + CEEGuiTextures.ENERGY_METER.getWidth() - 28;
        disconnectY = guiTop + 18;
        resetX = guiLeft + 6;
        resetY = guiTop + 18;
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        CEEGuiTextures.ENERGY_METER.render(graphics, x, y);
        (be.disconnected ? CEEGuiTextures.ENERGY_METER_OPEN : CEEGuiTextures.ENERGY_METER_CLOSE).render(graphics, disconnectX, disconnectY);

        if (be.owner == null || (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getUUID().equals(be.owner)))
            CEEGuiTextures.ENERGY_METER_RESET.render(graphics, resetX, resetY);

        int stringWidth = font.width(title);
        graphics.drawString(font, title, x - stringWidth / 2 + windowWidth / 2, y + 4, 0xFFFFEE);

        double totalEnergy = Mth.lerp((float) ((AnimationTickHolder.getTicks() + AnimationTickHolder.getPartialTicks()) - be.thisPacketTick) /(be.thisPacketTick-be.lastPacketTick), be.oldTotalEnergy, be.totalEnergy);

        if (totalEnergy < 0)
            totalEnergy = 10000000 + totalEnergy;

        float number0 = (float) (totalEnergy * 100 % 1);
        float number1 = (float) (totalEnergy * 10 % 1);
        float number2 = (float) (totalEnergy % 1);
        float number3 = (float) (totalEnergy / 10 % 1);
        float number4 = (float) (totalEnergy / 100 % 1);
        float number5 = (float) (totalEnergy / 1000 % 1);
        float number6 = (float) (totalEnergy / 10000 % 1);
        float number7 = (float) (totalEnergy / 100000 % 1);
        float number8 = (float) (totalEnergy / 1000000 % 1);
        float number9 = (float) (totalEnergy / 10000000 % 1);

//        number0 = Math.round(number0 * 10) / 10f;
//        number1 = Math.round(number1 * 10) / 10f;
//        number2 = Math.round(number2 * 10) / 10f;
//        number3 = Math.round(number3 * 10) / 10f;
//        number4 = Math.round(number4 * 10) / 10f;
//        number5 = Math.round(number5 * 10) / 10f;
//        number6 = Math.round(number6 * 10) / 10f;
//        number7 = Math.round(number7 * 10) / 10f;
//        number8 = Math.round(number8 * 10) / 10f;
//        number9 = Math.round(number9 * 10) / 10f;


        graphics.blit(CreateElecrtoEnergetics.rl("textures/gui/energy_meter.png"), x + 185, y + 27, 0, 243, 210 - (number0 * 210) + 6, 13, 30, 256, 256);
        graphics.blit(CreateElecrtoEnergetics.rl("textures/gui/energy_meter.png"), x + 168, y + 27, 0, 243, 210 - (number1 * 210) + 6, 13, 30, 256, 256);
        graphics.blit(CreateElecrtoEnergetics.rl("textures/gui/energy_meter.png"), x + 151, y + 27, 0, 243, 210 - (number2 * 210) + 6, 13, 30, 256, 256);
        graphics.blit(CreateElecrtoEnergetics.rl("textures/gui/energy_meter.png"), x + 128, y + 27, 0, 243, 210 - (number3 * 210) + 6, 13, 30, 256, 256);
        graphics.blit(CreateElecrtoEnergetics.rl("textures/gui/energy_meter.png"), x + 111, y + 27, 0, 243, 210 - (number4 * 210) + 6, 13, 30, 256, 256);
        graphics.blit(CreateElecrtoEnergetics.rl("textures/gui/energy_meter.png"), x + 94, y + 27, 0, 243, 210 - (number5 * 210) + 6, 13, 30, 256, 256);
        graphics.blit(CreateElecrtoEnergetics.rl("textures/gui/energy_meter.png"), x + 77, y + 27, 0, 243, 210 - (number6 * 210) + 6, 13, 30, 256, 256);
        graphics.blit(CreateElecrtoEnergetics.rl("textures/gui/energy_meter.png"), x + 60, y + 27, 0, 243, 210 - (number7 * 210) + 6, 13, 30, 256, 256);
        graphics.blit(CreateElecrtoEnergetics.rl("textures/gui/energy_meter.png"), x + 43, y + 27, 0, 243, 210 - (number8 * 210) + 6, 13, 30, 256, 256);
        graphics.blit(CreateElecrtoEnergetics.rl("textures/gui/energy_meter.png"), x + 26, y + 27, 0, 243, 210 - (number9 * 210) + 6, 13, 30, 256, 256);

        GuiGameElement.of(CEEBlocks.ENERGY_METER.asItem()).
                        <GuiGameElement.GuiRenderBuilder>at(guiLeft + CEEGuiTextures.ENERGY_METER.getWidth() + 6, guiTop + CEEGuiTextures.ENERGY_METER.getHeight() - 56, 100)
                .scale(5)
                .render(graphics);

        if ((be.owner == null || (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getUUID().equals(be.owner))) &&
            mouseX > resetX && mouseX <= resetX + 15 &&
            mouseY > resetY && mouseY <= resetY + 15) {
            graphics.renderComponentTooltip(font,
                    List.of(
                            Lang.builder(CreateElecrtoEnergetics.ID).translate("gui.energy_meter.reset").component(),
                            Lang.builder(CreateElecrtoEnergetics.ID).translate("gui.energy_meter.reset_tip_1")
                                    .style(ChatFormatting.DARK_GRAY)
                                    .style(ChatFormatting.ITALIC).component()
                    ), mouseX, mouseY);
        }

        if (mouseX > disconnectX && mouseX <= disconnectX + 15 &&
            mouseY > disconnectY && mouseY <= disconnectY + 15) {
            if (be.owner == null || (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getUUID().equals(be.owner)))
                graphics.renderComponentTooltip(font,
                        List.of(
                                Lang.builder(CreateElecrtoEnergetics.ID).translate(be.disconnected ? "gui.energy_meter.disconnected" : "gui.energy_meter.connected")
                                        .component(),
                                Lang.builder(CreateElecrtoEnergetics.ID).translate(be.disconnected ? "gui.energy_meter.connect" : "gui.energy_meter.disconnect")
                                        .style(ChatFormatting.GRAY).component(),
                                Lang.builder(CreateElecrtoEnergetics.ID).translate("gui.energy_meter.disconnect_tip_1")
                                        .style(ChatFormatting.DARK_GRAY)
                                        .style(ChatFormatting.ITALIC).component()
                        ), mouseX, mouseY);
            else
                graphics.renderComponentTooltip(font,
                        List.of(
                                Lang.builder(CreateElecrtoEnergetics.ID).translate(be.disconnected ? "gui.energy_meter.disconnected" : "gui.energy_meter.connected")
                                        .component()
                        ), mouseX, mouseY);
        }

    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean lmb = button == GLFW.GLFW_MOUSE_BUTTON_LEFT;

        // Disconnect / Connect
        if ((be.owner == null || (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getUUID().equals(be.owner))) &&
                lmb && mouseX > disconnectX && mouseX <= disconnectX + 15 &&
                mouseY > disconnectY && mouseY <= disconnectY + 15) {
            CatnipServices.NETWORK.sendToServer(new ChangeEnergyMeterStatePacket(false, !be.disconnected, be.getBlockPos()));
        }

        // Reset
        if ((be.owner == null || (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getUUID().equals(be.owner))) &&
                lmb && mouseX > resetX && mouseX <= resetX + 15 &&
                mouseY > resetY && mouseY <= resetY + 15) {
            CatnipServices.NETWORK.sendToServer(new ChangeEnergyMeterStatePacket(true, be.disconnected, be.getBlockPos()));
        }


        return super.mouseClicked(mouseX, mouseY, button);
    }
}
