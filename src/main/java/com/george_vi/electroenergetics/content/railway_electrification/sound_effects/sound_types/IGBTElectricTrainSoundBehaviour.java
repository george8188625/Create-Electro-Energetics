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
    ElectricTrainSoundInstance decayStaticSoundInstance;

    float prevTrainSpeed;
    Phasing phasing = Phasing.ASYNC;

    @Override
    public void tick() {
        super.tick();
        trainSpeed *= 1.05f;

        //NORMAL
        if (trainSpeed != 0 && phasing == Phasing.ASYNC && (staticStartSoundInstance == null || staticStartSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(staticStartSoundInstance)))
            staticStartSoundInstance = playSound(pos, CEESoundEvents.TRAIN_IGBT_ASYNC_START.get());
        if (trainSpeed != 0 && phasing == Phasing.ASYNC && (staticSoundInstance == null || staticSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(staticSoundInstance)))
            staticSoundInstance = playSound(pos, CEESoundEvents.TRAIN_IGBT_ASYNC.get());
        if (trainSpeed != 0 && phasing == Phasing.ASYNC && (decaySoundInstance == null || decaySoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(decaySoundInstance)))
            decaySoundInstance = playSound(pos, CEESoundEvents.TRAIN_IGBT_ASYNC_DECAY.get());
        if (trainSpeed != 0 && phasing == Phasing.ASYNC && (decayStaticSoundInstance == null || decayStaticSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(decayStaticSoundInstance)))
            decayStaticSoundInstance = playSound(pos, CEESoundEvents.TRAIN_IGBT_ASYNC_DECAY2.get());
        if (trainSpeed != 0 && (mainSoundInstance == null || mainSoundInstance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(mainSoundInstance)))
            mainSoundInstance = playSound(pos, CEESoundEvents.TRAIN_GTO_ASYNC_RISE.get());

        float trainSpeedNormalized = (4 * trainSpeed) / (3 * (AllConfigs.server().trains.poweredTrainTopSpeed.getF() / 20)) / 1.05f;
        //Minecraft.getInstance().gui.setOverlayMessage(Component.literal(String.format("%.2f", trainSpeedNormalized)), false);
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

        if (decaySoundInstance != null) { // ASYNC DECAY 1
            if (acceleration > 0) {
                if (trainSpeedNormalized < 0.357f) {
                    decaySoundInstance.setVolumeImmediately(Math.max(0, (10f * trainSpeedNormalized) * 1 - 0.6f));
                }

                if (trainSpeedNormalized < 0.14f) { //Async Static
                    decaySoundInstance.setPitchImmediately(1f) ;
                }

                if (trainSpeedNormalized >= 0.14f && trainSpeedNormalized < 0.357f) { //Async Slowrise
                    decaySoundInstance.setPitchImmediately(Mth.lerp(4.5f * (trainSpeedNormalized - 0.14f), 1f, 1.12f));
                }

                if (trainSpeedNormalized >= 0.357f && trainSpeedNormalized < 0.44f) { //Async Quickrise
                    decaySoundInstance.setPitchImmediately(Mth.lerp(18f * (trainSpeedNormalized - 0.357f), 1.12f, 2f));
                    decaySoundInstance.setVolumeImmediately(Math.max(-120f * (trainSpeedNormalized - 0.357f) + 2.4f, 0));
                }
            }

            if (acceleration < 0) {
                if (trainSpeedNormalized < 0.44f) {
                    decaySoundInstance.setVolumeImmediately(Math.max(0, (10f * trainSpeedNormalized) * 1 - 0.6f));
                    decaySoundInstance.setPitchImmediately(1f);
                }
            }

            decaySoundInstance.setPos(pos);
            decaySoundInstance.keepAlive();
            if (phasing != Phasing.ASYNC || trainSpeed == 0) {
                Minecraft.getInstance().getSoundManager().stop(decaySoundInstance);
                decaySoundInstance = null;
            }
        }

        if (staticStartSoundInstance != null) { // ASYNC STATIC START
            //Minecraft.getInstance().gui.setOverlayMessage(Component.literal(String.format("%.2f", staticSoundInstance.targetVolume)), false);
            staticStartSoundInstance.setPitchImmediately(1f);
            if (acceleration > 0) {
                if (trainSpeedNormalized < 0.05f) {
                    staticStartSoundInstance.setVolumeImmediately(Math.min(trainSpeedNormalized * 50, 1f));
                }

                if (trainSpeedNormalized >= 0.05f && trainSpeedNormalized < 0.15f) {
                    staticStartSoundInstance.setVolumeImmediately(Math.max((-25/14) * (trainSpeedNormalized - (14/25)) , 0));
                }
            }
            else {
                staticStartSoundInstance.setVolumeImmediately(0f);
            }

            staticStartSoundInstance.setPos(pos);
            staticStartSoundInstance.keepAlive();
            if (phasing != Phasing.ASYNC || trainSpeed == 0) {
                Minecraft.getInstance().getSoundManager().stop(staticStartSoundInstance);
                staticStartSoundInstance = null;
            }
        }

        if (decayStaticSoundInstance != null) { // ASYNC STATIC DECAY (DECAY 2)
            if (acceleration > 0) {
                if (trainSpeedNormalized < 0.14f) { //Async Static
                    decayStaticSoundInstance.setPitchImmediately(1f);
                }

                if (trainSpeedNormalized >= 0.14f && trainSpeedNormalized < 0.37f) { //Async Slowrise
                    decayStaticSoundInstance.setPitchImmediately(Mth.lerp(4.5f * (trainSpeedNormalized - 0.14f), 1f, 1.12f));
                }

                if (trainSpeedNormalized < 0.357f) {
                    decayStaticSoundInstance.setVolumeImmediately(Math.min(2.4f * trainSpeedNormalized / 0.357f, 2.4f));
                }

                if (trainSpeedNormalized >= 0.357f && trainSpeedNormalized < 0.44f) { //Async Quickrise
                    decayStaticSoundInstance.setPitchImmediately(Mth.lerp(18f * (trainSpeedNormalized - 0.357f), 1.12f, 2f));
                    decayStaticSoundInstance.setVolumeImmediately(Math.max(-40f * (trainSpeedNormalized - 0.357f) + 2.4f, 0));
                }
            }

            if (acceleration < 0) {
                if (trainSpeedNormalized < 0.44f) {
                    decayStaticSoundInstance.setVolumeImmediately(Math.min(2.4f * trainSpeedNormalized / 0.357f, 2.4f));
                    decayStaticSoundInstance.setPitchImmediately(1f);
                }
            }

            decayStaticSoundInstance.setPos(pos);
            decayStaticSoundInstance.keepAlive();
            if (phasing != Phasing.ASYNC || trainSpeed == 0) {
                Minecraft.getInstance().getSoundManager().stop(decayStaticSoundInstance);
                decayStaticSoundInstance = null;
            }
        }

        if (staticSoundInstance != null) { // ASYNC STATIC
            //Minecraft.getInstance().gui.setOverlayMessage(Component.literal(String.format("%.2f", staticSoundInstance.targetVolume)), false);
            if (acceleration > 0) {
                if (trainSpeedNormalized < 0.05f) {
                    staticSoundInstance.setVolumeImmediately(Math.min(2.7f * trainSpeedNormalized / 0.05f, 2.7f));
                }

                if (trainSpeedNormalized >= 0.05f && trainSpeedNormalized < 0.357f) {
                    staticSoundInstance.setVolumeImmediately(Math.max((-2.7f/0.2f) * (trainSpeedNormalized - 0.44f), 0));
                }

                if (trainSpeedNormalized < 0.14f) { //Async Static
                    staticSoundInstance.setPitchImmediately(1f);
                }

                if (trainSpeedNormalized >= 0.14f && trainSpeedNormalized < 0.37f) { //Async Slowrise
                    staticSoundInstance.setPitchImmediately(Mth.lerp(4.5f * (trainSpeedNormalized - 0.14f), 1f, 1.12f));
                }

                if (trainSpeedNormalized >= 0.357f && trainSpeedNormalized < 0.44f) {//Async Quickrise
                    staticSoundInstance.setPitchImmediately(Mth.lerp(18f * (trainSpeedNormalized - 0.357f), 1.12f, 2f));
                    staticSoundInstance.setVolumeImmediately(Math.max(-20f * (trainSpeedNormalized - 0.357f) + 1f, 0));
                }
            }
            if (acceleration < 0) {
                if (trainSpeedNormalized < 0.05f) { //Async Static
                    staticSoundInstance.setPitchImmediately(1f);
                    staticSoundInstance.setVolumeImmediately(Math.min(2.7f * trainSpeedNormalized / 0.05f, 2.7f));
                    }
                if (trainSpeedNormalized >= 0.05f && trainSpeedNormalized < 0.44f) {
                    staticSoundInstance.setPitchImmediately(1f);
                    staticSoundInstance.setVolumeImmediately(Math.max((-2.7f / 0.2f) * (trainSpeedNormalized - 0.44f), 0));
                }
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
        // if mainSoundInstance is for some reason null, this prevents an NPE (somehow the sound inside can be null too)
        //noinspection ConstantValue
        newInstance.setPitchImmediately(mainSoundInstance == null || mainSoundInstance.getSound() == null ? 1 : mainSoundInstance.getPitch());
        newInstance.setVolumeImmediately(3);

        if (mainSoundInstance != null)
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
