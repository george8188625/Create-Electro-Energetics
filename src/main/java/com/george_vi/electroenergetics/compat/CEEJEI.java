package com.george_vi.electroenergetics.compat;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.content.electrical_panel.link.ElectricalPanelLinkScreen;
import com.simibubi.create.compat.jei.GhostIngredientHandler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
@SuppressWarnings("unused")
public class CEEJEI implements IModPlugin {

    public static final ResourceLocation ID = CreateElectroEnergetics.rl("jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerIngredientAliases(@NotNull IIngredientAliasRegistration registration) {
        registration.addAlias(VanillaTypes.ITEM_STACK, CEEBlocks.SF6_BREAKER.asStack(), "sf6");
        registration.addAlias(VanillaTypes.ITEM_STACK, CEEBlocks.HV_SWITCH.asStack(), "hv switch");
        registration.addAlias(VanillaTypes.ITEM_STACK, CEEBlocks.HV_CAPACITOR.asStack(), "hv capacitor");
        registration.addAlias(VanillaTypes.ITEM_STACK, CEEBlocks.ALTERNATOR_ROTOR.asStack(), "generator");
        registration.addAlias(VanillaTypes.ITEM_STACK, CEEBlocks.STATOR.asStack(), "generator");
        registration.addAlias(VanillaTypes.ITEM_STACK, CEEBlocks.ALTERNATOR_BRUSHES.asStack(), "generator");
        registration.addAlias(VanillaTypes.ITEM_STACK, CEEBlocks.THREE_PHASE_ALTERNATOR_BRUSHES.asStack(), "generator");

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(ElectricalPanelLinkScreen.class, new GhostIngredientHandler());
    }
}
