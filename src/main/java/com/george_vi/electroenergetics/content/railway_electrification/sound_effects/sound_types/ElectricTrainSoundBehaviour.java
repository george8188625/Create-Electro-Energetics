package com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ElectricTrainSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
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
            windRiseSoundInstance = playSound(pos, CEESoundEvents.TRAIN_WIND_RISE.get());
        if (trainSpeed != 0 && (windSoundInstance == null || windSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(windSoundInstance)))
            windSoundInstance = playSound(pos, CEESoundEvents.TRAIN_WIND_STATIC.get());

        if (trainSpeed != 0) {
            windSoundInstance.targetVolume = trainSpeed * 1;
            windRiseSoundInstance.targetVolume = trainSpeed * 0.5f + 0.15f;
            windSoundInstance.targetPitch = 1f;
            windRiseSoundInstance.targetPitch = (trainSpeed * 0.3f) + 1f;
            windSoundInstance.setPos(pos);
            windRiseSoundInstance.setPos(pos);
            windSoundInstance.keepAlive();
            windRiseSoundInstance.keepAlive();
        }
    };

    public static ElectricTrainSoundInstance playSound(Vec3 pos, SoundEvent event) {
        ElectricTrainSoundInstance newInstance = new ElectricTrainSoundInstance(pos, event);
        // For some reason when you set the volume before playing the sound, it's louder from farther away??
        newInstance.setVolumeImmediately(3f);
        Minecraft.getInstance().getSoundManager().play(newInstance);
        // And then you can turn it down, and it's not gonna be super quiet when it's made louder after some time??
        newInstance.setVolumeImmediately(0f);
        return newInstance;
    }
}
