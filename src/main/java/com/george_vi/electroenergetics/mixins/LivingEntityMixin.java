package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEEMobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract boolean addEffect(MobEffectInstance effectInstance);

    @Inject(method = "checkTotemDeathProtection", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/LivingEntity;removeEffectsCuredBy(Lnet/neoforged/neoforge/common/EffectCure;)Z"), remap = false)
    public void checkTotemDeathProtection(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        this.addEffect(new MobEffectInstance(CEEMobEffects.DIELECTRIC, 600, 0));
    }
}
