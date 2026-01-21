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
        if (trainSpeed == 0) {
            this.prevSpeed = trainSpeed;
            return;
        }

        if (mainSoundInstance == null || mainSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(mainSoundInstance))
            Minecraft.getInstance().getSoundManager().play(mainSoundInstance = new ElectricTrainSoundInstance(pos, CEESoundEvents.DC_TRAIN.get()));

        int tth = (int) Math.max(Math.floor(trainSpeed > 0.2 ? 8 * trainSpeed + 5 : 32 * trainSpeed), -1);
        if (acceleration < 0)
            tth -= 1;
        int th = pth;
        if (th > tth)
            th = Math.max(tth, th - 3);
        else if (th < tth)
            th = Math.min(tth, th + 3);

        if (th != pth)
            Minecraft.getInstance().level.playLocalSound(pos.x, pos.y, pos.z, CEESoundEvents.TRAIN_RELAY.get(), SoundSource.NEUTRAL, 0.4f, 1f, false);
        if (prevSpeed == 0)
            Minecraft.getInstance().level.playLocalSound(pos.x, pos.y, pos.z, CEESoundEvents.DC_TRAIN_START.get(), SoundSource.NEUTRAL, 0.2f, 1f, false);
        mainSoundInstance.setPos(pos);

        mainSoundInstance.targetPitch = Math.min(0.56f, trainSpeed) * 1.3f + 0.7f;
        mainSoundInstance.targetVolume = (trainSpeed < 0.3 ? (trainSpeed * 3) : (trainSpeed + 0.9f)) * (acceleration > 0 ? 2f : acceleration < 0 ? 0.25f : 0.125f);

        mainSoundInstance.keepAlive();
        this.pth = th;
        this.prevSpeed = trainSpeed;
    }
}
