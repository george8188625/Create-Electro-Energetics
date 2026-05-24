package com.george_vi.electroenergetics.events.datagen;

import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.MechanicalCraftingRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class CEEMechanicalCraftingRecipeGen extends MechanicalCraftingRecipeGen {

    GeneratedRecipe LINEMANS_STICK = create(CEEItems.LINEMANS_STICK::get).returns(1)
            .recipe(b -> b
                    .key('N', CEETags.IRON_NUGGET)
                    .key('R', AllItems.PRECISION_MECHANISM.get())
                    .key('H', AllItems.BRASS_HAND.get())
                    .key('S', Tags.Items.RODS_WOODEN)
                    .key('D', Items.RED_DYE)
                    .patternLine("NRH")
                    .patternLine(" S ")
                    .patternLine(" S ")
                    .patternLine(" S ")
                    .patternLine(" D ")
                    .disallowMirrored());

    public CEEMechanicalCraftingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, CreateElectroEnergetics.ID);
    }


}
