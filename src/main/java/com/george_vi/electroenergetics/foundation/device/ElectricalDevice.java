package com.george_vi.electroenergetics.foundation.device;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public interface ElectricalDevice {

    /**
     * Shows smoking particles around the specified pos.
     */
    static void showOverheatingParticles(Level level, BlockPos pos) {
        if (!level.isLoaded(pos))
            return;
        Vec3 pPos = pos.getCenter();

        if (level.random.nextFloat() > 0.5f)
            ((ServerLevel)level).sendParticles(ParticleTypes.SMOKE, pPos.x, pPos.y, pPos.z, 5, 0.2, 0.2, 0.2, 0);
    }

    /**
     * Handles device explosion and smoking.
     */
    static void handleTemp(Level level, BlockPos pos, DevicesSavedData deviceSD,
                            float temp, float smokeThreshold, float breakThreshold) {
        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (temp > breakThreshold) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40,
                        new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION,
                        pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f,
                        0, 0, 0,0, 0);
            }
            deviceSD.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (temp > smokeThreshold)
            ElectricalDevice.showOverheatingParticles(level, pos);
    }

    /**
     * When heat > 0, the temperature rises, until it settles into a value. That value depends on the heat value.
     * This is the formula for the value, the temperature settles into, where h - heat
     * max(0, 30 * (h - 3.3))
     * This is similar to the temperature mentioned in WireType, but the values are different.
     * Temperature here isn't based on just the current, but power (for instance heat loss calculated using the P=I²R formula).
     *
     * @param temp temp value in abstract units
     * @param heat heat (energy loss) in Watts
     * @return new temp value in abstract units
     */
    static float updateTemp(float temp, float heat) {
        if (!Float.isFinite(temp))
            temp = 0;

        float newTemp = heat + 30f;
        newTemp *= Math.min(temp < 0 ? 0 : 1 / (1 + (temp / 1000)), 1);
        newTemp = Math.max(temp - 33.3f + newTemp, 0);

        return newTemp;
    }

    /**
     * @return the final temp value this heat will converge to in abstract units.
     */
    static float finalTempAt(float heat) {
        return (float) Math.max(0, 30 * (heat - 3.3));
    }
}
