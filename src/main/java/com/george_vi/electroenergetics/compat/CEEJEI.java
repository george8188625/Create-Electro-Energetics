package com.george_vi.electroenergetics.compat;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.library.ingredients.itemStacks.TypedItemStack;
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
        registration.addAlias(TypedItemStack.create(CEEBlocks.SF6_BREAKER.asStack()), "sf6");
        registration.addAlias(TypedItemStack.create(CEEBlocks.HV_SWITCH.asStack()), "hv switch");
        registration.addAlias(TypedItemStack.create(CEEBlocks.HV_CAPACITOR.asStack()), "hv capacitor");
        registration.addAlias(TypedItemStack.create(CEEBlocks.ALTERNATOR_ROTOR.asStack()), "generator");
        registration.addAlias(TypedItemStack.create(CEEBlocks.STATOR.asStack()), "generator");
        registration.addAlias(TypedItemStack.create(CEEBlocks.ALTERNATOR_BRUSHES.asStack()), "generator");
        registration.addAlias(TypedItemStack.create(CEEBlocks.THREE_PHASE_ALTERNATOR_BRUSHES.asStack()), "generator");
        registration.addAlias(TypedItemStack.create(CEEBlocks.THREE_PHASE_ALTERNATOR_BRUSHES.asStack()), "generator");

    }
}
