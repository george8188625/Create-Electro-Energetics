package com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

public class ElectricTrainSoundType {
    @OnlyIn(Dist.CLIENT)
    public Supplier<ElectricTrainSoundBehaviour> soundBehaviour;
    public final ResourceLocation id;
    public final Supplier<Object2IntMap<Block>> validBlocks;
    public ElectricTrainSoundType(Supplier<Supplier<ElectricTrainSoundBehaviour>> soundBehaviour, ResourceLocation id, Supplier<Object2IntMap<Block>> validBlocks) {
        this.validBlocks = validBlocks;
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> this.soundBehaviour = soundBehaviour.get());
        this.id = id;
    }

    public String translationKey() {
        return id.getNamespace() + ".electric_train_sound." + id.getPath();
    }
}
