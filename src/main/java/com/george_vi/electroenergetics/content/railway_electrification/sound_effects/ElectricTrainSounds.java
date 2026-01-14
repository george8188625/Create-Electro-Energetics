package com.george_vi.electroenergetics.content.railway_electrification.sound_effects;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundBehaviour;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundType;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.trains.entity.Train;
import net.createmod.catnip.data.Pair;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ElectricTrainSounds {
    static Map<Pair<UUID, Integer>, ElectricTrainSoundEntry> soundProperties = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    static Map<Pair<UUID, Integer>, ElectricTrainSoundBehaviour> sounds = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        for (Map.Entry<Pair<UUID, Integer>, ElectricTrainSoundEntry> e : Set.copyOf(soundProperties.entrySet())) {
            Pair<UUID, Integer> carriagePair = e.getKey();
            UUID trainID = carriagePair.getFirst();
            int carriageID = carriagePair.getSecond();
            Train train = CreateClient.RAILWAYS.trains.get(trainID);
            if (train == null) {
                sounds.remove(carriagePair);
                soundProperties.remove(carriagePair);
                continue;
            }
            ElectricTrainSoundBehaviour soundBehaviour = sounds.get(carriagePair);
            if (soundBehaviour == null)
                sounds.put(carriagePair, soundBehaviour = e.getValue().type().soundBehaviour.get());

            soundBehaviour.pos = e.getValue().pos();
            soundBehaviour.trainSpeed = e.getValue().speed();
            soundBehaviour.acceleration = e.getValue().acceleration();

            if (e.getValue().ticks() > 0) {
                soundBehaviour.tick();
                soundProperties.replace(carriagePair, new ElectricTrainSoundEntry(e.getValue().pos(), e.getValue().speed(), e.getValue().acceleration(), e.getValue().active(), e.getValue().ticks() - 1, e.getValue().type()));
            } else
                soundProperties.remove(carriagePair);
        }
    }

    @Unique
    public static void addSMBlock(Block block, Set<TrainSoundModifier> smBlocks) {
        for (ElectricTrainSoundType soundType : CEERegistries.ELECTRIC_TRAIN_SOUND_TYPE) {
            if (soundType.validBlocks.get().containsKey(block)) {
                smBlocks.add(new TrainSoundModifier(block, soundType.validBlocks.get().getInt(block), soundType));
                return;
            }
        }
    }
}
