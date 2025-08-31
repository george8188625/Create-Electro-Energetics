package com.george_vi.electroenergetics.content.railway_electrification.sound_effects;

import net.minecraft.world.phys.Vec3;

public record ElectricTrainSoundEntry(Vec3 pos, float speed, float acceleration, boolean active) {
}
