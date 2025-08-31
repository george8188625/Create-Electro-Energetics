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
import java.util.UUID;

public class ElectricTrainSounds {
    static Map<UUID, ElectricTrainSoundEntry> soundProperties = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    static Map<UUID, ElectricTrainSoundInstance> sounds = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        for (Map.Entry<UUID, ElectricTrainSoundEntry> e : soundProperties.entrySet()) {
            UUID trainID = e.getKey();
            Train train = CreateClient.RAILWAYS.trains.get(trainID);
            if (train == null) {
                sounds.remove(trainID);
                soundProperties.remove(trainID);
                continue;
            }
            Vec3 pos = e.getValue().pos();

            ElectricTrainSoundInstance instance = sounds.get(trainID);
            if (instance == null || instance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(instance)) {
                instance = new ElectricTrainSoundInstance(pos);
                Minecraft.getInstance().getSoundManager().play(instance);
                sounds.put(trainID, instance);
            }

            instance.setPos(pos);
            float trainSpeed = e.getValue().speed();
            float acceleration = e.getValue().acceleration();

            instance.targetPitch = (float) trainSpeed * 2f + 0.4f;
            instance.targetVolume = e.getValue().active() && trainSpeed > 0.01 ? Math.max(0, acceleration) * 600 + 0.6f : 0;
            instance.keepAlive();
        }
    }

}
