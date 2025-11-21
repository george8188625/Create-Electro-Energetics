package com.george_vi.electroenergetics.content.buzzer;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class BuzzerBlockEntity extends SmartBlockEntity {
    double voltage = 0;

    @OnlyIn(Dist.CLIENT)
    private ElectricHumSoundInstance soundInstance;

    public BuzzerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        CatnipServices.PLATFORM.executeOnClientOnly(() -> this::tickAudio);
    }

    @OnlyIn(Dist.CLIENT)
    void tickAudio() {
        if (soundInstance == null || soundInstance.isStopped()) {
            Minecraft.getInstance().getSoundManager()
                    .play(soundInstance = new ElectricHumSoundInstance(CEESoundEvents.BUZZER.get(), worldPosition));
        }
        soundInstance.setPitch(1f);
        soundInstance.keepAlive();
        soundInstance.setVolumeImmediately((float) Mth.clamp(Mth.lerp(voltage / 100f, -0.04f, 1.4f), 0f, 1.4f));
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putDouble("Voltage", voltage);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        voltage = tag.getDouble("Voltage");
    }

    public void setVoltage(double voltage) {
        if (Math.abs(this.voltage - voltage) < 0.1)
            return;
        this.voltage = voltage;
        sendData();
    }
}
