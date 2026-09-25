package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ElectricTrainSounds;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.CarriageSounds;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CarriageSounds.class)
public class CarriageSoundsMixin {
    @Shadow
    CarriageContraptionEntity entity;

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/AllSoundEvents$SoundEntry;playAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;FFZ)V"), remap = false)
    private void electroEnergetics$cancelSteam(AllSoundEvents.SoundEntry instance, Level world, Vec3 pos, float volume, float pitch, boolean fade, Operation<Void> original) {
        if (instance != AllSoundEvents.STEAM || ElectricTrainSounds.soundProperties.keySet().stream().noneMatch(p -> p.getFirst().equals(entity.getCarriage().train.id)))
            original.call(instance, world, pos, volume, pitch, fade);
    }
}
