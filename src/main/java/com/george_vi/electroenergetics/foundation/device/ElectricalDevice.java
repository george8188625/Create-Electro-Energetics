package com.george_vi.electroenergetics.foundation.device;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface ElectricalDevice {

    /**
     * Shows smoking particles around the specified pos.
     */
    default void showOverheatingParticles(Level level, BlockPos pos) {
        if (!level.isLoaded(pos))
            return;
        Vec3 pPos = pos.getCenter();

        if (level.random.nextFloat() > 0.5f)
            ((ServerLevel)level).sendParticles(ParticleTypes.SMOKE, pPos.x, pPos.y, pPos.z, 5, 0.2, 0.2, 0.2, 0);
    }

    /**
     * When heat > 0, the temperature rises, until it settles into a value. That value depends on the heat value.
     * This is the formula for the value, the temperature settles into, where h - heat
     * max(0,30 * (h - 3.3))
     * This is similar to the temperature mentioned in WireType, but the values are different.
     * Temperature here isn't based on just the current, but power (for instance heat loss calculated using the P=I²R formula).
     *
     * @param temp temp value in abstract units
     * @param heat heat (energy loss) in Watts
     * @return new temp value in abstract units
     */
    default float updateTemp(float temp, float heat) {
        if (Float.isNaN(temp))
            temp = 0;

        float newTemp = heat + 30f;
        newTemp *= Math.min(temp < 0 ? 0 : 1 / (1 + (temp / 1000)), 1);
        newTemp = Math.max(temp - 33.3f + newTemp, 0);

        return newTemp;
    }
}
