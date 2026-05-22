package com.george_vi.electroenergetics.events.datagen;

import com.george_vi.electroenergetics.CEEFluids;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.simibubi.create.api.data.recipe.MixingRecipeGen;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.material.Fluids;

import java.util.concurrent.CompletableFuture;

public class CEEMixingRecipeGen extends MixingRecipeGen {
    public CEEMixingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, CreateElectroEnergetics.ID);
    }

    GeneratedRecipe TRANSFORMER_OIL = create("transformer_oil", b -> b
            .require(CEETags.PLANT_OIL, 250)
            .require(Fluids.WATER, 250)
            .output(CEEFluids.TRANSFORMER_OIL.get(), 125)
            .requiresHeat(HeatCondition.HEATED));
}
