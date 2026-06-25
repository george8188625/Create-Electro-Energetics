package com.george_vi.electroenergetics.content.electrical_panel.special_interaction;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class CEEHoldInteractionHandler {
    private static HoldInteractionBehavior interaction;

    public static boolean isInteracting() {
        return interaction != null;
    }

    @Nullable
    public static HoldInteractionBehavior getInteractionPos() {
        return interaction;
    }

    public static void startInteraction(HoldInteractionBehavior interaction) {
        CEEHoldInteractionHandler.interaction = interaction;
    }

    public static void release() {
        if (interaction != null)
            interaction.release();
        interaction = null;
    }

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.options.keyUse.isDown() ||
                (interaction != null && !interaction.isStillActive()))
            release();
        else if (interaction != null)
            interaction.tick();
    }

    public static void onPlayerMove(double x, double y) {
        if (interaction != null)
            interaction.onMouseMove(x, y);
    }
}
