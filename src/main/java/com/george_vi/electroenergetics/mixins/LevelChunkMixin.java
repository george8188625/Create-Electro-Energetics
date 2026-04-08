package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.devices.device.DeviceBlock;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {

    @Shadow
    @Final
    private Level level;

    @Inject(method = "setBlockState", at = @At("TAIL"), remap = false)
    public void setBlockState(BlockPos pos, BlockState state, boolean isMoving, CallbackInfoReturnable<BlockState> cir) {
        if (!(level instanceof ServerLevel level))
            return;

        if (state.getBlock() instanceof DeviceBlock<?> db) {
            SimulatedDeviceType<?> type = db.getDevice();
            DevicesSavedData sd = DevicesSavedData.load(level);
            sd.addDevice(type, pos, db.getDefaultDeviceData(level, pos, state));
            return;
        }
//
//        SimulatedDeviceType<?> type = SimulatedDeviceType.BY_BLOCK.get(state.getBlock());
//        if (type == null)
//            return;
//
//        DevicesSavedData sd = DevicesSavedData.load(level);
//        sd.addDevice(type, pos, new CompoundTag());
    }
}
