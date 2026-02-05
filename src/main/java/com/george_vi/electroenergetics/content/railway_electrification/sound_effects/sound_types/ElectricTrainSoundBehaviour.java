package com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ElectricTrainSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ElectricTrainSoundBehaviour {
    public ElectricTrainSoundInstance windRiseSoundInstance;
    public ElectricTrainSoundInstance windSoundInstance;

    public float trainSpeed;
    public float acceleration;
    public Vec3 pos;

    @OnlyIn(Dist.CLIENT)
    public void tick() {
        // Wind

        if (trainSpeed != 0 && (windRiseSoundInstance == null || windRiseSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(windRiseSoundInstance)))
            Minecraft.getInstance().getSoundManager().play(windRiseSoundInstance = new ElectricTrainSoundInstance(pos, CEESoundEvents.TRAIN_WIND_RISE.get()));
        if (trainSpeed != 0 && (windSoundInstance == null || windSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(windSoundInstance)))
            Minecraft.getInstance().getSoundManager().play(windSoundInstance = new ElectricTrainSoundInstance(pos, CEESoundEvents.TRAIN_WIND_STATIC.get()));

        if (trainSpeed != 0) {
            windSoundInstance.targetVolume = trainSpeed * 3;
            windRiseSoundInstance.targetVolume = trainSpeed * 2 + 0.5f;
            windSoundInstance.targetPitch = 1f;
            windRiseSoundInstance.targetPitch = (trainSpeed * 0.3f) + 1f;
            windSoundInstance.setPos(pos);
            windRiseSoundInstance.setPos(pos);
            windSoundInstance.keepAlive();
            windRiseSoundInstance.keepAlive();
        }
    };
}
