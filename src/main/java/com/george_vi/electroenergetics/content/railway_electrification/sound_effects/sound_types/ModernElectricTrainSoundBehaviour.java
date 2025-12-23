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
        if (mainSoundInstance == null || mainSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(mainSoundInstance))
            Minecraft.getInstance().getSoundManager().play(mainSoundInstance = new ElectricTrainSoundInstance(pos, CEESoundEvents.ELECTRIC_TRAIN.get()));

        mainSoundInstance.setPos(pos);
        backgroundSoundInstance.setPos(pos);

        mainSoundInstance.targetPitch = Math.min(0.7f, trainSpeed) * 1.3f + 0.4f;
        mainSoundInstance.targetVolume = trainSpeed > 0.01 ? ((acceleration == 0 ? 0.4f : (acceleration > 0 ? acceleration : acceleration / -8) * 600) + 0.2f) / (1 + trainSpeed * 3) : 0;
        mainSoundInstance.targetVolume /= 2;

        backgroundSoundInstance.targetPitch = trainSpeed * 2f + 0.4f;
        backgroundSoundInstance.targetVolume = trainSpeed + 0.1f;

        mainSoundInstance.keepAlive();
        backgroundSoundInstance.keepAlive();
        this.prevTrainSpeed = trainSpeed;
    }
}
