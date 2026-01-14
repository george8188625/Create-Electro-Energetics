package com.george_vi.electroenergetics.content.railway_electrification.sound_effects;

import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundType;
import net.minecraft.world.level.block.Block;

import java.util.Objects;

public final class TrainSoundModifier {
    private final Block block;
    private final int priority;
    private final ElectricTrainSoundType soundType;

    public TrainSoundModifier(Block block, int priority, ElectricTrainSoundType soundType) {
        this.block = block;
        this.priority = priority;
        this.soundType = soundType;
    }

    public Block block() {
        return block;
    }

    public int priority() {
        return priority;
    }

    public ElectricTrainSoundType soundType() {
        return soundType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TrainSoundModifier) obj;
        return Objects.equals(this.block, that.block) &&
                this.priority == that.priority &&
                Objects.equals(this.soundType, that.soundType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, priority, soundType);
    }

    @Override
    public String toString() {
        return "TrainSoundModifier[" +
                "block=" + block + ", " +
                "priority=" + priority + ", " +
                "soundType=" + soundType + ']';
    }

}
