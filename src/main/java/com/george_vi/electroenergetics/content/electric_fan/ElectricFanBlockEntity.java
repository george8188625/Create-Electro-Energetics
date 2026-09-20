package com.george_vi.electroenergetics.content.electric_fan;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ElectricFanBlockEntity extends SmartBlockEntity implements IAirCurrentSource {
    float actualSpeed;
    float targetSpeed;
    float rotation = 0;
    float prevRotation = 0;

    public AirCurrent airCurrent;
    protected int airCurrentUpdateCooldown;
    protected int entitySearchCooldown;
    protected boolean updateAirFlow;

    @OnlyIn(Dist.CLIENT)
    protected ElectricHumSoundInstance windSoundInstance;

    public ElectricFanBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        airCurrent = new AirCurrent(this);
        updateAirFlow = true;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public void tick() {
        float prevSpeed = actualSpeed;
        actualSpeed = calculateSpeedChange(actualSpeed, targetSpeed);

        prevRotation = rotation;
        rotation += actualSpeed * 30f;
        if (rotation > 360 || rotation < 360) {
            float diff = Mth.floor(Math.abs(rotation) / 360) * Math.signum(rotation) * 360;
            rotation -= diff;
            prevRotation -= diff;
        }


        if (Math.abs(this.actualSpeed - prevSpeed) > (Math.abs(actualSpeed) > 1 ? 1 : 0.1) ||
                (actualSpeed == 0) != (prevSpeed == 0)) {
            updateAirFlow = true;
            updateChute();
        }

        if (level.isClientSide)
            CatnipServices.PLATFORM.executeOnClientOnly(() -> this::tickAudio);

        // From EncasedFanBlockEntity#tick

        boolean server = !level.isClientSide || isVirtual();

        if (server && airCurrentUpdateCooldown-- <= 0) {
            airCurrentUpdateCooldown = AllConfigs.server().kinetics.fanBlockCheckRate.get();
            updateAirFlow = true;
        }

        if (updateAirFlow) {
            updateAirFlow = false;
            airCurrent.rebuild();
            if (airCurrent.maxDistance > 0)
                award(AllAdvancements.ENCASED_FAN);
            sendData();
        }

        if (getSpeed() == 0)
            return;

        if (entitySearchCooldown-- <= 0) {
            entitySearchCooldown = 5;
            airCurrent.findEntities();
        }

        airCurrent.tick();
    }

    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        float speedNormalized = Math.abs(actualSpeed) / 10f;
        if (Math.abs(speedNormalized) < 0.1f)
            return;

        speedNormalized = speedNormalized * speedNormalized;

        if (windSoundInstance == null || windSoundInstance.isStopped()) {
            Minecraft.getInstance()
                    .getSoundManager()
                    .play(windSoundInstance = new ElectricHumSoundInstance(CEESoundEvents.TRAIN_WIND_STATIC.get(), worldPosition));
        } else if (windSoundInstance != null && speedNormalized != 0) {
            windSoundInstance.keepAlive();
            windSoundInstance.setVolume(speedNormalized * 0.4f);
            windSoundInstance.setPitch(Mth.lerp(speedNormalized, 0.1f, 2f));
        }
    }

    public void setSpeed(float speed) {
        if (Math.abs(this.targetSpeed - speed) > (Math.abs(targetSpeed) > 1 ? 0.1 : 0.01) ||
                (targetSpeed == 0) != (speed == 0)) {
            this.targetSpeed = speed;
            if (!level.isClientSide)
                sendData();
        }
    }


    @Override
    public Direction getAirflowOriginSide() {
        return this.getBlockState()
                .getValue(ElectricFanBlock.FACING);
    }

    @Override
    public Direction getAirFlowDirection() {
        if (actualSpeed == 0)
            return null;
        Direction facing = getBlockState().getValue(ElectricFanBlock.FACING);
        return actualSpeed > 0 ? facing : facing.getOpposite();
    }

    @Override
    public boolean isSourceRemoved() {
        return remove;
    }

    @Override
    public AirCurrent getAirCurrent() {
        return airCurrent;
    }

    @Nullable
    @Override
    public Level getAirCurrentWorld() {
        return level;
    }

    @Override
    public BlockPos getAirCurrentPos() {
        return worldPosition;
    }

    @Override
    public float getSpeed() {
        return Math.abs(actualSpeed) > 1 ? actualSpeed * actualSpeed * 0.8f : 0;
    }

    @Override
    public void remove() {
        super.remove();
        updateChute();
    }

    public void updateChute() {
        Direction direction = getBlockState().getValue(ElectricFanBlock.FACING);
        if (!direction.getAxis()
                .isVertical())
            return;
        BlockEntity poweredChute = level.getBlockEntity(worldPosition.relative(direction));
        if (!(poweredChute instanceof ChuteBlockEntity chuteBE))
            return;
        if (direction == Direction.DOWN)
            chuteBE.updatePull();
        else
            chuteBE.updatePush(1);
    }

    public void blockInFrontChanged() {
        updateAirFlow = true;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        actualSpeed = tag.getFloat("Speed");
        targetSpeed = tag.getFloat("TargetSpeed");
        if (clientPacket)
            airCurrent.rebuild();
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("Speed", actualSpeed);
        tag.putFloat("TargetSpeed", targetSpeed);
    }

    public static float calculateSpeedChange(float speed, float target) {
        return Mth.lerp(Math.abs(speed) < 1 ? 0.1f : 0.01f, speed, Mth.clamp(target, -10, 10));
    }
}
