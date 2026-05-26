package com.george_vi.electroenergetics.events.datagen;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.advancement.CriterionTriggerBase;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class SimpleCEETrigger extends CEECriterionTriggerBase<SimpleCEETrigger.Instance> {
    public SimpleCEETrigger(String id) {
        super(id);
    }

    public void trigger(ServerPlayer player) {
        super.trigger(player, null);
    }

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public Instance instance() {
        return new Instance();
    }

    public static class Instance extends CEECriterionTriggerBase.Instance {
        private static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player)
        ).apply(instance, Instance::new));

        private final Optional<ContextAwarePredicate> player;

        public Instance() {
            player = Optional.empty();
        }

        public Instance(Optional<ContextAwarePredicate> player) {
            this.player = player;
        }

        @Override
        protected boolean test(@Nullable List<Supplier<Object>> suppliers) {
            return true;
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return player;
        }
    }
}
