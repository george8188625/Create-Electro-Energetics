package com.george_vi.electroenergetics.content.railway_electrification.sound_effects;

import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ElectricTrainSoundBehaviour {
    public float trainSpeed;
    public float acceleration;
    public Vec3 pos;

    @OnlyIn(Dist.CLIENT)
    public abstract void tick();
}
