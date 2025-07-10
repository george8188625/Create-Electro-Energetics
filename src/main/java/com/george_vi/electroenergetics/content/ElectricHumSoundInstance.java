package com.george_vi.electroenergetics.content;

import com.george_vi.electroenergetics.CEESoundEvents;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class ElectricHumSoundInstance extends AbstractTickableSoundInstance {

    private boolean active;
    private int keepAlive;

    public ElectricHumSoundInstance(BlockPos pos) {
        super(CEESoundEvents.HUM.get(), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());

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

    public void keepAlive() {
        keepAlive = 2;
    }

    @Override
    public void tick() {
        if (active) {
            volume = Math.min(4, volume + .25f);
            keepAlive--;
            if (keepAlive == 0)
                active =  false;
            return;

        }
        volume = Math.max(0, volume - .25f);
        if (volume == 0)
            stop();
    }
}
