package com.george_vi.electroenergetics.content.railway_electrification.sound_effects;

import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundType;
import net.minecraft.world.phys.Vec3;

public record ElectricTrainSoundEntry(float speed, float acceleration, boolean active, int ticks, ElectricTrainSoundType type) {
}
