package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.simulation.simulator.MicroTickedSimulationTicker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "tickServer", at = @At(value = "HEAD"), remap = false)
    public void electroEnergetics$tickServerPre(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        MicroTickedSimulationTicker.profiler.clear();
        MicroTickedSimulationTicker.allStats.clear();
        for (ServerLevel level : ((MinecraftServer)(Object)this).getAllLevels())
            MicroTickedSimulationTicker.tick(level);
    }

    @Inject(method = "tickServer", at = @At(value = "TAIL"), remap = false)
    public void electroEnergetics$tickServerPost(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        for (ServerLevel level : ((MinecraftServer)(Object)this).getAllLevels())
            MicroTickedSimulationTicker.endTick(level);
    }

    @Inject(method = "runServer", at = @At(value = "HEAD"), remap = false)
    public void electroEnergetics$runServer(CallbackInfo ci) {
        MicroTickedSimulationTicker.runServer();
    }

    @Inject(method = "runServer", at = @At(value = "TAIL"), remap = false)
    public void electroEnergetics$stopServer(CallbackInfo ci) {
        MicroTickedSimulationTicker.stopServer();
    }
}
