package com.george_vi.electroenergetics;

import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public enum CEEGuiTextures implements ScreenElement, TextureSheetSegment {
    ENERGY_METER("energy_meter", 232, 120),
    ENERGY_METER_RESET("energy_meter",195, 121, 15, 16),
    ENERGY_METER_OPEN("energy_meter",211, 121, 15, 16),
    ENERGY_METER_CLOSE("energy_meter",227, 121, 15, 16);

    public final ResourceLocation location;
    private final int width;
    private final int height;
    private final int startX;
    private final int startY;

    CEEGuiTextures(String name, int width, int height) {
        this(name, 0, 0, width, height);
    }

    CEEGuiTextures(String name, int startX, int startY, int width, int height) {
        this.location = CreateElectroEnergetics.rl("textures/gui/" + name + ".png");
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
    }


    @Override
    public int getStartX() {
        return startX;
    }

    @Override
    public int getStartY() {
        return startY;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, startX, startY, width, height);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y, Color c) {
        bind();
        UIRenderHelper.drawColoredTexture(graphics, c, x, y, startX, startY, width, height);
    }

    @Override
    public ResourceLocation getLocation() {
        return location;
    }
}
