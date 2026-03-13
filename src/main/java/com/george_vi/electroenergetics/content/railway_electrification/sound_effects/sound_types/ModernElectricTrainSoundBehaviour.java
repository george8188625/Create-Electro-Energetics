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
                mainSoundInstance.setPitchImmediately(Math.min(2, (trainSpeed * 0.5f) + 0.35f));
                mainSoundInstance.setVolumeImmediately((acceleration > 0 ? 1 : 0.01f) * (trainSpeedNormalized - 0.22f));
            } else {
                mainSoundInstance.setPitchImmediately(Math.min(2, (trainSpeed * 0.5f) + 0.35f));
                mainSoundInstance.setVolumeImmediately(acceleration > 0 ? 3f : 0.7f);
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
		/*DO NOT USE THIS PUSH, THIS PUSH IS SIMPLY FOR ILLUSTRATIVE PURPOSES*/
		
		/*How long the phases are should be dependent to the top speed, so if the top speed of a train is higher each phase would take longer.*/
        ASYNC(0, 0.4f, CEESoundEvents.TRAIN_GTO_ASYNC_RISE.get()),    /*This should be 6/40 of the top speed*/
        P15(0.4f, 0.6f, CEESoundEvents.TRAIN_GTO_P15.get()),          /*This should be 6/40 of the top speed*/
        P9(0.6f, 1.1f, CEESoundEvents.TRAIN_GTO_P9.get()),            /*This should be 8/40 of the top speed*/
		                                                              /*This is missing P5, which should be 9/40 of the top speed*/
        P3(1.1f, 1.5f, CEESoundEvents.TRAIN_GTO_P3.get()),            /*This should be 5/40 of the top speed*/
        P1(1.5f, Float.MAX_VALUE, CEESoundEvents.TRAIN_GTO_P1.get()); /*This should be 6/40 of the top speed*/
		
		/* A bar to illustrate what I'm talking about here          */
		/* Let Top Speed = t                                        */
		/* Speed:   0    0.15t   0.3t     0.5t    0.725t  0.85t   t */
		/*          |------|------|--------|---------|-----|------| */
		/* Phase:   |Async |P15   |P9      |P5       |P3   |P1    | */
		
		
		/*The rate in which the pitch of the audio increases/decreases should also be dependent to the top speed*/
		/* Let Top Speed = t                                                */
		/* Speed:             0   0.1t              0.55t                t  */
		/* (x means muted)->  |xxxx|------------------|------------------|  */
		/* Pitch Change:          1/4                 0                  2  */ 
		/*                         v                  v                  v  */
		/* Hz:                    75                 300                600 */
		/*                                        (Original)                */
		
		
		/*For reference on how the sounds should be played https://drive.google.com/file/d/197_oQuFk4IiKZy9jo3iZX1reDImpaVfr/view?usp=sharing my audacity project file when creating the original audio*/
		/*Feel free to ask me on Discord for any questions you may have*/

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
