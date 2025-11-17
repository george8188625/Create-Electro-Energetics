package com.george_vi.electroenergetics;

import com.simibubi.create.foundation.damageTypes.DamageTypeBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public class CEEDamageTypes {
    public static final ResourceKey<DamageType>
            ELECTROCUTION = key("electrocution"),
            HV_ELECTROCUTION = key("hv_electrocution");

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, CreateElecrtoEnergetics.rl(name));
    }

    public static void bootstrap(BootstrapContext<DamageType> ctx) {
        new DamageTypeBuilder(ELECTROCUTION).scaling(DamageScaling.NEVER).register(ctx);
        new DamageTypeBuilder(HV_ELECTROCUTION).scaling(DamageScaling.NEVER).register(ctx);
    }
}
