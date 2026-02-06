package com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ElectricTrainSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;

public class ModernElectricTrainSoundBehaviour extends ElectricTrainSoundBehaviour {
    ElectricTrainSoundInstance mainSoundInstance;
    ElectricTrainSoundInstance backgroundSoundInstance;

    float prevTrainSpeed;
    Phasing phasing = Phasing.ASYNC;

    @Override
    public void tick() {
        super.tick();
        trainSpeed *= 1.05f;

        if (trainSpeed != 0 && phasing == Phasing.ASYNC && (backgroundSoundInstance == null || backgroundSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(backgroundSoundInstance)))
            backgroundSoundInstance = playSound(pos, CEESoundEvents.TRAIN_GTO_ASYNC.get());
        if (trainSpeed != 0 && (mainSoundInstance == null || mainSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(mainSoundInstance)))
            mainSoundInstance = playSound(pos, CEESoundEvents.TRAIN_GTO_ASYNC_RISE.get());


        if (trainSpeed != 0) {
            switchPhasing();
            if (phasing == Phasing.ASYNC) {
                float trainSpeedNormalized = trainSpeed / 0.4f;
//                mainSoundInstance.setPitchImmediately(Math.min(2, (trainSpeed * 0.75f) + 0.5f));
                mainSoundInstance.setPitchImmediately(Math.min(2, (trainSpeed * 0.5f) + 0.35f));

//                mainSoundInstance.setPitchImmediately(trainSpeedNormalized + 0.05f);
                mainSoundInstance.setVolumeImmediately((trainSpeedNormalized - 0.22f));
            } else {
                mainSoundInstance.setPitchImmediately(Math.min(2, (trainSpeed * 0.5f) + 0.35f));
                mainSoundInstance.setVolumeImmediately(3f);
//                mainSoundInstance.targetVolume = trainSpeed * 4 + 0.3f;
            }
            mainSoundInstance.setPos(pos);
            mainSoundInstance.keepAlive();
        }

        if (backgroundSoundInstance != null) {
            float trainSpeedNormalized = trainSpeed / 0.4f;
            backgroundSoundInstance.setPitchImmediately(1f);
            backgroundSoundInstance.targetVolume = Math.min(trainSpeedNormalized * 5, 3);
            backgroundSoundInstance.setPos(pos);
            backgroundSoundInstance.keepAlive();
            if (phasing != Phasing.ASYNC || trainSpeed == 0) {
                Minecraft.getInstance().getSoundManager().stop(backgroundSoundInstance);
                backgroundSoundInstance = null;
            }
        }
        this.prevTrainSpeed = trainSpeed;
    }

    void switchPhasing() {
        Phasing newPhasing = Phasing.ASYNC;
        for (int i = 0; i < Phasing.values().length; i++) {
            Phasing ph = Phasing.values()[i];
            if (ph.startingSpeed < trainSpeed && ph.endingSpeed > trainSpeed) {
                newPhasing = ph;
                break;
            }
        }

        if (phasing == newPhasing)
            return;

        phasing = newPhasing;
        ElectricTrainSoundInstance newInstance = new ElectricTrainSoundInstance(pos, phasing.soundEvent);
        newInstance.setPitchImmediately(mainSoundInstance.getPitch());
        newInstance.setVolumeImmediately(3);
        Minecraft.getInstance().getSoundManager().stop(mainSoundInstance);
        Minecraft.getInstance().getSoundManager().play(mainSoundInstance = newInstance);


    }

    private enum Phasing {
        ASYNC(0, 0.4f, CEESoundEvents.TRAIN_GTO_ASYNC_RISE.get()),
        P15(0.4f, 0.6f, CEESoundEvents.TRAIN_GTO_P15.get()),
        P9(0.6f, 1.1f, CEESoundEvents.TRAIN_GTO_P9.get()),
        P3(1.1f, 1.5f, CEESoundEvents.TRAIN_GTO_P3.get()),
        P1(1.5f, Float.MAX_VALUE, CEESoundEvents.TRAIN_GTO_P1.get());

        public final float startingSpeed;
        public final float endingSpeed;
        public final SoundEvent soundEvent;

        Phasing(float startingSpeed, float endingSpeed, SoundEvent soundEvent) {
            this.startingSpeed = startingSpeed;
            this.endingSpeed = endingSpeed;
            this.soundEvent = soundEvent;
        }
    }
}
