package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;
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
        SimulationTicker.profiler.clear();
        SimulationTicker.allStats.clear();

        for (ServerLevel level : ((MinecraftServer)(Object)this).getAllLevels())
            InfrastructureSavedData.load(level).ticker.tick();
    }

    @Inject(method = "tickServer", at = @At(value = "TAIL"), remap = false)
    public void electroEnergetics$tickServerPost(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        for (ServerLevel level : ((MinecraftServer)(Object)this).getAllLevels()) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(level);
            sd.ticker.endTick();
            // It's called after the simulation, because certain devices can "move" ownership of certain devices into
            // micro-tickers, which causes devices to not move the data correctly when moving across Sable's sublevels,
            // as the fields would be written to while the simulation is still running.
            DevicesSavedData dsd = sd.deviceSD;
            dsd.tick();
        }
    }

    @Inject(method = "runServer", at = @At(value = "HEAD"), remap = false)
    public void electroEnergetics$runServer(CallbackInfo ci) {
        SimulationTicker.runServer();
    }

    @Inject(method = "runServer", at = @At(value = "TAIL"), remap = false)
    public void electroEnergetics$stopServer(CallbackInfo ci) {
        SimulationTicker.stopServer();
    }
}
