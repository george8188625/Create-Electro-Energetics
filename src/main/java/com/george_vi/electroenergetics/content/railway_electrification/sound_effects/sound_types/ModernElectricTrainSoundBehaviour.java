package com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ElectricTrainSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ModernElectricTrainSoundBehaviour extends ElectricTrainSoundBehaviour {
    ElectricTrainSoundInstance mainSoundInstance;
    ElectricTrainSoundInstance backgroundSoundInstance;
    float prevTrainSpeed;
    @Override
    public void tick() {

        if (backgroundSoundInstance == null || backgroundSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(backgroundSoundInstance))
            Minecraft.getInstance().getSoundManager().play(backgroundSoundInstance = new ElectricTrainSoundInstance(pos, CEESoundEvents.ELECTRIC_TRAIN_BACKGROUND.get()));
        if (trainSpeed != 0 && (mainSoundInstance == null || mainSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(mainSoundInstance)))
            Minecraft.getInstance().getSoundManager().play(mainSoundInstance = new ElectricTrainSoundInstance(pos, CEESoundEvents.ELECTRIC_TRAIN.get()));


        backgroundSoundInstance.setPos(pos);
        if (trainSpeed != 0) {
            if (trainSpeed < 0.4) {
                mainSoundInstance.targetPitch = Math.min(0.34f, trainSpeed) * 1.5f + 0.4f;
            } else if (prevTrainSpeed < 0.4) {
                mainSoundInstance.setPitchImmediately(Math.min(0.6f, trainSpeed) * 1.3f + 0.5f);
            } else if (trainSpeed < 0.6) {
             mainSoundInstance.targetPitch = Math.min(0.6f, trainSpeed) * 1.3f + 0.5f;
            } else if (prevTrainSpeed < 0.6) {
                mainSoundInstance.setPitchImmediately(Math.min(0.9f, trainSpeed) * 1.3f + 0.0f);
            } else
                mainSoundInstance.targetPitch = Math.min(0.7f, trainSpeed) * 1.3f + 0.0f;
            mainSoundInstance.setPos(pos);
            mainSoundInstance.targetVolume = trainSpeed > 0.01 ? ((acceleration == 0 ? 0.4f : (acceleration > 0 ? acceleration : acceleration / -8) * 600) + 0.2f) / (1 + trainSpeed * 3) : 0;
            mainSoundInstance.targetVolume /= 2;
            mainSoundInstance.keepAlive();
        }

        backgroundSoundInstance.targetPitch = trainSpeed * 2f + 0.4f;
        backgroundSoundInstance.targetVolume = trainSpeed + 0.1f;

        backgroundSoundInstance.keepAlive();
        this.prevTrainSpeed = trainSpeed;
    }
}
