package com.george_vi.electroenergetics.events.datagen;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static com.george_vi.electroenergetics.events.datagen.CEEAdvancement.TaskType.*;


public class CEEAdvancements implements DataProvider {


    public static final List<CEEAdvancement> ENTRIES = new ArrayList<>();
    public static final CEEAdvancement START = null,

    ROOT = create("root", b -> b.icon(CEEBlocks.CONNECTOR)
            .title("Create: Electro Energetics")
            .description("Welcome to the world of Electricity")
            .awardedForFree()
            .special(SILENT)),

    CONNECT_WIRES = create("connect_wires", b -> b.icon(CEEItems.WIRE_SPOOL)
            .title("Wired Differently")
            .description("Connect a wire between two nodes")
            .after(ROOT)
            .special(NOISY)),

    SHORT_CIRCUIT = create("short_circuit", b -> b.icon(CEEItems.COPPER_WIRE)
            .title("Certified Electrician")
            .description("Cause a Short Circuit")
            .after(CONNECT_WIRES)
            .special(SECRET)),

    FUSE_BYPASS = create("fuse_bypass", b -> b.icon(Items.COPPER_INGOT)
            .title("When did his house burn down?")
            .description("Use a fuse bypass")
            .after(CONNECT_WIRES)
            .special(SECRET)),

    TRANSFORMER = create("transformer", b -> b.icon(CEEBlocks.TRANSFORMER_CORE)
            .title("Step up, Step down")
            .description("Create a transformer")
            .whenItemCollected(CEEBlocks.TRANSFORMER_CORE)
            .after(CONNECT_WIRES));

    private static CEEAdvancement create(String id, UnaryOperator<CEEAdvancement.Builder> b) {
        CEEAdvancement advancement = new CEEAdvancement(id, b);
        ENTRIES.add(advancement);
        return advancement;
    }

    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public CEEAdvancements(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return this.registries.thenCompose(provider -> {
            PackOutput.PathProvider pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "advancement");
            List<CompletableFuture<?>> futures = new ArrayList<>();

            Set<ResourceLocation> set = new HashSet<>();
            Consumer<AdvancementHolder> consumer = (advancement) -> {
                ResourceLocation id = advancement.id();
                if (!set.add(id))
                    throw new IllegalStateException("Duplicate advancement " + id);
                Path path = pathProvider.json(id);
                futures.add(DataProvider.saveStable(cache, provider, Advancement.CODEC, advancement.value(), path));
            };

            for (CEEAdvancement advancement : ENTRIES)
                advancement.save(consumer, provider);

            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    public static void provideLang(BiConsumer<String, String> consumer) {
        for (CEEAdvancement advancement : ENTRIES)
            advancement.provideLang(consumer);
    }

    public static void registerTrigger() {
        for (CEEAdvancement advancement : ENTRIES)
            if (advancement.builtinTrigger != null)
                Registry.register(BuiltInRegistries.TRIGGER_TYPES, advancement.builtinTrigger.getId(), advancement.builtinTrigger);

    }

    @Override
    public String getName() {
        return "CEE's Advancements";
    }
}
