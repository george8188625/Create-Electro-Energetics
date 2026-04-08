package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {


    @Shadow
    protected abstract BlockState asState();

    @Inject(method = "onRemove", at = @At("TAIL"), remap = false)
    public void onRemove(Level l, BlockPos pos, BlockState newState, boolean movedByPiston, CallbackInfo ci) {
        if (!(l instanceof ServerLevel level))
            return;

        DevicesSavedData sd = DevicesSavedData.load(level);
        BlockState oldState = asState();
        if (newState.getBlock() != oldState.getBlock()) {
            SimulatedDevice device = sd.getDevice(pos);
            if (device == null || !device.shouldRemove(oldState, newState))
                return;
            sd.removeDevice(pos);
        }
    }
}
