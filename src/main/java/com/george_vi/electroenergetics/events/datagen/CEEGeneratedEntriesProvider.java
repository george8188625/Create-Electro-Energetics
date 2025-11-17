package com.george_vi.electroenergetics.events.datagen;

import com.george_vi.electroenergetics.CEEDamageTypes;
import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CEEGeneratedEntriesProvider extends DatapackBuiltinEntriesProvider {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, CEEDamageTypes::bootstrap);
    public CEEGeneratedEntriesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(CreateElecrtoEnergetics.ID));

    }

    @Override
    public String getName() {
        return "CEE Registries";
    }
}
