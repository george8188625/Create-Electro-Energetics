package com.george_vi.electroenergetics.content.railway_electrification.sound_effects;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.trains.entity.Train;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ElectricTrainSounds {
    static Map<UUID, ElectricTrainSoundEntry> soundProperties = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    static Map<UUID, Couple<ElectricTrainSoundInstance>> sounds = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        for (Map.Entry<UUID, ElectricTrainSoundEntry> e : Set.copyOf(soundProperties.entrySet())) {
            UUID trainID = e.getKey();
            Train train = CreateClient.RAILWAYS.trains.get(trainID);
            if (train == null) {
                sounds.remove(trainID);
                soundProperties.remove(trainID);
                continue;
            }
            Vec3 pos = e.getValue().pos();

            Couple<ElectricTrainSoundInstance> instances = sounds.get(trainID);
            if (instances == null || instances.either(i -> i == null || i.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(i))) {
                ElectricTrainSoundInstance instanceMain = new ElectricTrainSoundInstance(pos, true);
                ElectricTrainSoundInstance instanceBackground = new ElectricTrainSoundInstance(pos, false);
                Minecraft.getInstance().getSoundManager().play(instanceMain);
                Minecraft.getInstance().getSoundManager().play(instanceBackground);
                sounds.put(trainID, instances = Couple.create(instanceMain, instanceBackground));
            }
            ElectricTrainSoundInstance instanceMain = instances.getFirst();
            ElectricTrainSoundInstance instanceBackground = instances.getSecond();

            instanceMain.setPos(pos);
            instanceBackground.setPos(pos);
            float trainSpeed = e.getValue().speed();
            float acceleration = e.getValue().acceleration();

            instanceMain.targetPitch = trainSpeed * 1.3f + 0.3f;
            instanceMain.targetVolume = e.getValue().active() && trainSpeed > 0.01 ? Math.max(0, acceleration) * 600 + 1f : 0;

            instanceBackground.targetPitch = trainSpeed * 2f + 0.4f;
            instanceBackground.targetVolume = e.getValue().active() ? 1f : 0;

            if (e.getValue().ticks() > 0) {
                instanceMain.keepAlive();
                instanceBackground.keepAlive();
                soundProperties.replace(trainID, new ElectricTrainSoundEntry(e.getValue().pos(), e.getValue().speed(), e.getValue().acceleration(), e.getValue().active(), e.getValue().ticks() - 1));
            } else {
                soundProperties.remove(trainID);
            }
        }
    }

}
