package com.george_vi.electroenergetics.content;

import com.george_vi.electroenergetics.CEESoundEvents;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ElectricHumSoundInstance extends AbstractTickableSoundInstance {

    private boolean active;
    private int keepAlive;
    private float targetedVolume;

    public ElectricHumSoundInstance(SoundEvent event, BlockPos pos) {
        super(event, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());

        looping = true;
        active = true;
        volume = 0.05f;
        delay = 0;
        keepAlive();
        Vec3 v = Vec3.atCenterOf(pos);
        x = v.x;
        y = v.y;
        z = v.z;
    }

    public ElectricHumSoundInstance(BlockPos pos) {
        this(CEESoundEvents.HUM.get(), pos);
    }

    public void keepAlive() {
        keepAlive = 4;
    }

    public void setVolume(float volume) {
        targetedVolume = volume;
    }

    @Override
    public void tick() {
        if (active) {
            volume = Mth.lerp(0.5f, volume, targetedVolume);
            keepAlive--;
            if (keepAlive == 0)
                active =  false;
            return;

        }
        volume = Math.max(0, volume - .125f);
        if (volume == 0)
            stop();
    }

    public void setPitch(float v) {
        pitch = v;
    }

    public void setVolumeImmediately(float volume) {
        this.volume = volume;
    }
}
