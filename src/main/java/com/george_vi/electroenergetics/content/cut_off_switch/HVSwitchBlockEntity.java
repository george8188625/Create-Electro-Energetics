package com.george_vi.electroenergetics.content.cut_off_switch;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class HVSwitchBlockEntity extends SmartBlockEntity {
    public boolean arcing = false;
    float prevProgress = 0f;
    float progress = 0f;
    boolean connected = false;
    int arcTimer = 0;

    @OnlyIn(Dist.CLIENT)
    protected ElectricHumSoundInstance soundInstance;

    public HVSwitchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        prevProgress = progress;
        progress = Mth.clamp(progress + (connected ? 0.01f : -0.01f), 0, 1);
        if (!arcing)
            arcTimer = 0;
        else
            arcTimer++;
        if (!level.isClientSide)
            return;

        CatnipServices.PLATFORM.executeOnClientOnly(() -> this::tickAudio);
    }

    @OnlyIn(Dist.CLIENT)
    protected void tickAudio() {
        Vec3 connectorPos = Vec3.atCenterOf(getBlockPos()).add(0, 6/16f, 0)
                .add(Vec3.atLowerCornerOf(getBlockState().getValue(HVSwitchBlock.FACING).getNormal()).multiply(2, 0, 2));


        if (prevProgress != progress && (progress == 1 || progress == 0)) {
            level.playLocalSound(connectorPos.x, connectorPos.y, connectorPos.z, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1f,  connected ? 0.5f : 1f, false);
        }

        if (!arcing)
            return;

        if (soundInstance == null || soundInstance.isStopped())
            Minecraft.getInstance()
                    .getSoundManager()
                    .play(soundInstance = new ElectricHumSoundInstance(CEESoundEvents.ARC.get(), worldPosition));
        else if (soundInstance != null) {
            soundInstance.setVolume(1f);
            soundInstance.keepAlive();
        }

        float dX = Mth.cos(Mth.lerp(progress, Mth.HALF_PI, 0));
        float dY = Mth.sin(Mth.lerp(progress, Mth.HALF_PI, 0));
        float dX2 = Mth.cos(Mth.lerp(progress, -70, 0) / 180 * Mth.PI);
        float dY2 = Mth.sin(Mth.lerp(progress, -70, 0) / 180 * Mth.PI);

        Vec3 headPos = Vec3.atCenterOf(getBlockPos())
                .add(0, 5/16f + dY + dY2, 0)
                .add(Vec3.atLowerCornerOf(getBlockState().getValue(HVSwitchBlock.FACING).getNormal()).multiply(dX+dX2, 0, dX+dX2));


        float distance = (float) headPos.distanceTo(connectorPos);
        float instability = level.random.nextFloat() / 5 + 0.3f;

        for (int i = 0; i < distance * 20; i++) {
            float point = i / ((distance * 20) + 0.1f);
            Vec3 pPos = VecHelper.lerp(point, headPos, connectorPos)
                    .add(0, (1 - Math.pow((point * 2) - 1, 2)) * distance * instability, 0)
                    .offsetRandom(level.random, 0.3f);
            if (level.random.nextFloat() > 0.8)
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, pPos.x, pPos.y, pPos.z, 0, 0, 0);
            level.addParticle(ParticleTypes.BUBBLE_POP, pPos.x, pPos.y, pPos.z, 0, 0, 0);

        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        progress = tag.getFloat("Progress");
        connected = tag.getBoolean("Connected");
        arcing = tag.getBoolean("Arcing");
        arcTimer = tag.getInt("ArcTimer");
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("Progress", progress);
        tag.putBoolean("Connected", connected);
        tag.putBoolean("Arcing", arcing);
        tag.putInt("ArcTimer", arcTimer);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    protected AABB createRenderBoundingBox() {
        return AABB.encapsulatingFullBlocks(getBlockPos(), getBlockPos().relative(getBlockState().getValue(HVSwitchBlock.FACING), 2)).expandTowards(0, 1, 0);
    }
}
