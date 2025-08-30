package com.george_vi.electroenergetics.content.catenary;

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
    static Map<UUID, Pair<Vec3, Couple<Float>>> soundProperties = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    static Map<UUID, ElectricTrainSoundInstance> sounds = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        for (Map.Entry<UUID, Pair<Vec3, Couple<Float>>> e : soundProperties.entrySet()) {
            UUID trainID = e.getKey();
            Train train = CreateClient.RAILWAYS.trains.get(trainID);
            if (train == null) {
                sounds.remove(trainID);
                soundProperties.remove(trainID);
                continue;
            }
            Vec3 pos = e.getValue().getFirst();

            ElectricTrainSoundInstance instance = sounds.get(trainID);
            if (instance == null || instance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(instance)) {
                instance = new ElectricTrainSoundInstance(pos);
                Minecraft.getInstance().getSoundManager().play(instance);
                sounds.put(trainID, instance);
            }

            instance.setPos(pos);
            instance.targetVolume = e.getValue().getSecond().getFirst();
            instance.targetPitch = e.getValue().getSecond().getSecond();
            instance.keepAlive();
        }
    }

}
