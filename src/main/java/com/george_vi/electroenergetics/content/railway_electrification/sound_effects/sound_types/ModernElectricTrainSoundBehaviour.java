package com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ElectricTrainSoundBehaviour;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ElectricTrainSoundInstance;
import net.minecraft.client.Minecraft;

public class ModernElectricTrainSoundBehaviour extends ElectricTrainSoundBehaviour {
    ElectricTrainSoundInstance mainSoundInstance;
    ElectricTrainSoundInstance backgroundSoundInstance;
    @Override
    public void tick() {

        if (backgroundSoundInstance == null || backgroundSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(backgroundSoundInstance))
            Minecraft.getInstance().getSoundManager().play(backgroundSoundInstance = new ElectricTrainSoundInstance(pos, CEESoundEvents.ELECTRIC_TRAIN_BACKGROUND.get()));
        if (mainSoundInstance == null || mainSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(mainSoundInstance))
            Minecraft.getInstance().getSoundManager().play(mainSoundInstance = new ElectricTrainSoundInstance(pos, CEESoundEvents.ELECTRIC_TRAIN.get()));

        mainSoundInstance.setPos(pos);
        backgroundSoundInstance.setPos(pos);

        mainSoundInstance.targetPitch = trainSpeed * 1.3f + 0.3f;
        mainSoundInstance.targetVolume = trainSpeed > 0.01 ? Math.max(0, acceleration) * 600 + 1f : 0;

        backgroundSoundInstance.targetPitch = trainSpeed * 2f + 0.4f;
        backgroundSoundInstance.targetVolume = 1f;

        mainSoundInstance.keepAlive();
        backgroundSoundInstance.keepAlive();
    }
}
