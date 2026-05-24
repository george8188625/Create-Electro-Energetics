package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.content.linemans_stick.LinemansStickRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends LivingEntity> {

    @Inject(method = "setupAnim*", at = @At("RETURN"), remap = false)
    private void electroEnergetics$afterSetupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                                                  float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof AbstractClientPlayer player))
            return;

        LinemansStickRenderer.setupAnimAfter(player, (HumanoidModel<?>) (Object) this);
    }
}
