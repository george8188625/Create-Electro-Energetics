package com.george_vi.electroenergetics.content.railway_electrification.sound_effects;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ElectricTrainSoundInstance extends AbstractTickableSoundInstance {
    private boolean active;
    private int keepAlive;
    public float targetPitch;
    public float targetVolume;

    public ElectricTrainSoundInstance(Vec3 pos, SoundEvent event) {
        super(event, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        looping = true;
        active = true;
        volume = 0.05f;
        pitch = 0.05f;
        delay = 0;
        keepAlive();

        x = pos.x;
        y = pos.y;
        z = pos.z;
    }

    public void setVolumeImmediately(float volume) {
        this.volume = volume;
        this.targetVolume = volume;
    }

    public void setPitchImmediately(float pitch) {
        this.pitch = pitch;
        this.targetPitch = pitch;
    }

    public void keepAlive() {
        keepAlive = 2;
    }

    public void fadeOut() {
        this.active = false;
    }

    @Override
    public void tick() {
        if (active) {
            volume = Mth.lerp(0.1f, volume, targetVolume);
            pitch = Mth.lerp(0.1f, pitch, targetPitch);
            keepAlive--;
            if (keepAlive == 0)
                fadeOut();
            return;

        }
        volume = Math.max(0, volume - .25f);
        if (volume == 0)
            stop();
    }

    public void setPos(Vec3 pos) {
        x = pos.x;
        y = pos.y;
        z = pos.z;
    }
}
