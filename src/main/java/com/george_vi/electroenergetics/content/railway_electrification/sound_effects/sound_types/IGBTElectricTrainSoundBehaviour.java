package com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ElectricTrainSoundInstance;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;

public class IGBTElectricTrainSoundBehaviour extends ElectricTrainSoundBehaviour {
    ElectricTrainSoundInstance mainSoundInstance;
    ElectricTrainSoundInstance staticStartSoundInstance;
    ElectricTrainSoundInstance staticSoundInstance;
    ElectricTrainSoundInstance decaySoundInstance;

    float prevTrainSpeed;
    Phasing phasing = Phasing.ASYNC;

    @Override
    public void tick() {
        super.tick();
        trainSpeed *= 1.05f;

        if (trainSpeed != 0 && phasing == Phasing.ASYNC && (staticStartSoundInstance == null || staticStartSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(staticStartSoundInstance)))
            staticStartSoundInstance = playSound(pos, CEESoundEvents.TRAIN_IGBT_ASYNC_START.get());
        if (trainSpeed != 0 && phasing == Phasing.ASYNC && (staticSoundInstance == null || staticSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(staticSoundInstance)))
            staticSoundInstance = playSound(pos, CEESoundEvents.TRAIN_IGBT_ASYNC.get());
        if (trainSpeed != 0 && phasing == Phasing.ASYNC && (decaySoundInstance == null || decaySoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(decaySoundInstance)))
            decaySoundInstance = playSound(pos, CEESoundEvents.TRAIN_IGBT_ASYNC_DECAY.get());
        if (trainSpeed != 0 && (mainSoundInstance == null || mainSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(mainSoundInstance)))
            mainSoundInstance = playSound(pos, CEESoundEvents.TRAIN_GTO_ASYNC_RISE.get());

        float trainSpeedNormalized = (4 * trainSpeed) / (3 * (AllConfigs.server().trains.poweredTrainTopSpeed.getF() / 20)) / 1.05f;
//        Minecraft.getInstance().gui.setOverlayMessage(Component.literal(String.format("%.2f", trainSpeedNormalized)), false);
        if (trainSpeed != 0) {
            switchPhasing();

            if (phasing == Phasing.ASYNC) { // ASYNC RISE
                mainSoundInstance.setPitchImmediately(Mth.lerp((trainSpeedNormalized - 0.1f) * 20, 0.5f, 0.75f));
                mainSoundInstance.setVolumeImmediately(trainSpeedNormalized < 0.1 ? 0 : (trainSpeedNormalized - 0.1f) * (acceleration > 0 ? 20f : 10f));
            } else { // P15 - P1
                mainSoundInstance.setPitchImmediately(Math.min(2, Mth.lerp(trainSpeedNormalized, 0.25f, 2f)));
                mainSoundInstance.setVolumeImmediately(acceleration > 0 ? 10f : 0.7f);
            }
            mainSoundInstance.setPos(pos);
            mainSoundInstance.keepAlive();
        }

        if (decaySoundInstance != null) { // ASYNC DECAY
            decaySoundInstance.setVolumeImmediately(Math.max(0, (acceleration > 0 ? 10f : 0.7f) * (trainSpeedNormalized) * 1 - 0.6f));

            decaySoundInstance.setPitchImmediately(1);
//            decaySoundInstance.targetVolume = Math.min(trainSpeedNormalized * 20, 3);
            decaySoundInstance.setPos(pos);
            decaySoundInstance.keepAlive();
            if (phasing != Phasing.ASYNC || trainSpeed == 0) {
                Minecraft.getInstance().getSoundManager().stop(decaySoundInstance);
                decaySoundInstance = null;
            }
        }

        if (staticStartSoundInstance != null) { // ASYNC STATIC START
            staticStartSoundInstance.setPitchImmediately(1f);
            if (trainSpeedNormalized <= 0.01 || trainSpeed == 0) {
                staticStartSoundInstance.targetVolume = Math.min(trainSpeedNormalized * 300, 3);
            }

            if (trainSpeedNormalized > 0.01 || trainSpeed == 0 || trainSpeedNormalized < 0.076f) {
                staticStartSoundInstance.targetVolume = Math.max((-500/11) * (trainSpeedNormalized - 0.066f) , 0);
            }

            staticStartSoundInstance.setPos(pos);
            staticStartSoundInstance.keepAlive();
            if (phasing != Phasing.ASYNC || trainSpeed == 0) {
                Minecraft.getInstance().getSoundManager().stop(staticStartSoundInstance);
                staticStartSoundInstance = null;
            }
        }

        if (staticSoundInstance != null) { // ASYNC STATIC
            staticSoundInstance.setPitchImmediately(1f);
            if (trainSpeedNormalized <= 0.025 || trainSpeed == 0) {
                staticSoundInstance.targetVolume = Math.min(trainSpeedNormalized * 120, 3);
            }

            if (trainSpeedNormalized > 0.025 || trainSpeed == 0 || trainSpeedNormalized < 0.33) {
                staticSoundInstance.targetVolume = Math.max((-600/61) * (trainSpeedNormalized - 0.305f), 3);
            }

            staticSoundInstance.setPos(pos);
            staticSoundInstance.keepAlive();
            if (phasing != Phasing.ASYNC || trainSpeed == 0) {
                Minecraft.getInstance().getSoundManager().stop(staticSoundInstance);
                staticSoundInstance = null;
            }
        }
        this.prevTrainSpeed = trainSpeed;
    }

    void switchPhasing() {
        float trainSpeedNormalized = trainSpeed / (AllConfigs.server().trains.poweredTrainTopSpeed.getF() / 20);
        Phasing newPhasing = Phasing.ASYNC;
        for (int i = 0; i < Phasing.values().length; i++) {
            Phasing ph = Phasing.values()[i];
            if (ph.startingSpeed < trainSpeedNormalized && ph.endingSpeed > trainSpeedNormalized) {
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

        ASYNC(0, .33f, CEESoundEvents.TRAIN_GTO_ASYNC_RISE.get()),
        P3(.33f, .35f, CEESoundEvents.TRAIN_GTO_P3.get()),
        P1(.33f, Float.MAX_VALUE, CEESoundEvents.TRAIN_GTO_P1.get());

		/* Let Top Speed = t                                        */
		/* Speed:   0    0.15t   0.25t     0.45t    0.7t  0.8t    t */
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
