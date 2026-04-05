package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;
/** When the previous check in the original Method
    for a Lightning Rod in Range didn't return
anything, scan for electrical devices in this
area and prioritizes these over normal blocks
     */
@Mixin(ServerLevel.class)
@Debug(export = true)
public class ServerWorldMixin {


    /*@Inject(
            remap=false,
            method="Lnet/minecraft/server/level/ServerLevel;findLightningRod(Lnet/minecraft/core/BlockPos;)Ljava/util/Optional;",
            at=@At(value = "INVOKE", target="java.util.Optional.map")
    )*/
    public void enhanceLightningProbability(BlockPos lightningPos, CallbackInfoReturnable<Optional<BlockPos>> cir) {
        InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) (Object) this);
        sd.getDevices().stream().map(SimulatedDeviceInstance::pos)
                .filter(/*if device is in Range*/
                        devicePos->devicePos.closerThan(
                                new Vec3i(
                                        lightningPos.getX(),
                                        lightningPos.getY(),
                                        lightningPos.getZ()
                                ),100)
                ).sorted(/*if device is nearer than */
                        (pos1,pos2)->pos1.compareTo(
                                new Vec3i(
                                        pos2.getX(),
                                        pos2.getY(),
                                        pos2.getZ()
                                )))
                .findFirst().ifPresent(targetPos->{

                    cir.setReturnValue(Optional.of(targetPos));cir.cancel();
                });

    }


}
