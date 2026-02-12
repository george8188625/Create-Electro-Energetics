package com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ElectricTrainSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;

public class DCElectricTrainSoundBehaviour extends ElectricTrainSoundBehaviour {
    ElectricTrainSoundInstance mainSoundInstance;
    int pth = 0;
    float prevSpeed;
    @Override
    public void tick() {
        super.tick();
        if (trainSpeed == 0) {
            this.prevSpeed = trainSpeed;
            return;
        }

        if (mainSoundInstance == null || mainSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(mainSoundInstance))
            mainSoundInstance = playSound(pos, CEESoundEvents.DC_TRAIN.get());

        int tth = (int) Math.max(Math.floor(trainSpeed > 0.1 ? 4 * trainSpeed + 5 : 16 * trainSpeed), -1);
        if (acceleration < 0)
            tth = -1;
        int th = pth;
        if (th > tth)
            th = Math.max(tth, th - 3);
        else if (th < tth)
            th = Math.min(tth, th + 3);

        if (th != pth)
            Minecraft.getInstance().level.playLocalSound(pos.x, pos.y, pos.z, CEESoundEvents.TRAIN_RELAY.get(), SoundSource.NEUTRAL, 0.4f, 1f, false);
        if (prevSpeed == 0)
            Minecraft.getInstance().level.playLocalSound(pos.x, pos.y, pos.z, CEESoundEvents.DC_TRAIN_START.get(), SoundSource.NEUTRAL, 0.1f, 1f, false);
        mainSoundInstance.setPos(pos);

        mainSoundInstance.setPitchImmediately(Math.min(3f, trainSpeed) + 0.45f);
        if (acceleration < 0.001f)
            mainSoundInstance.targetVolume = trainSpeed * 0.04f;
        else if (trainSpeed < 0.3)
            mainSoundInstance.targetVolume = trainSpeed;
        else
            mainSoundInstance.targetVolume = 0.3f -(trainSpeed - 0.3f) * 0.03f;
        mainSoundInstance.targetVolume *= 5;

        mainSoundInstance.keepAlive();
        this.pth = th;
        this.prevSpeed = trainSpeed;
    }
}
