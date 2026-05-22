package com.george_vi.electroenergetics.events.datagen;

import com.george_vi.electroenergetics.CEEFluids;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.simibubi.create.api.data.recipe.CompactingRecipeGen;
import com.simibubi.create.api.data.recipe.MixingRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class CEECompactingRecipeGen extends CompactingRecipeGen {
    public CEECompactingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, CreateElectroEnergetics.ID);
    }

    GeneratedRecipe PLANT_OIL = create("plant_oil", b -> b
            .require(Tags.Items.SEEDS)
            .output(CEEFluids.PLANT_OIL.get(), 100));
}
