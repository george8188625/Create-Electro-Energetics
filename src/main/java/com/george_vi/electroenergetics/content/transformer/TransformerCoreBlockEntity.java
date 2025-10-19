package com.george_vi.electroenergetics.content.transformer;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.george_vi.electroenergetics.content.wire.WireRenderer;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.InWorldNode;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangNumberFormat;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransformerCoreBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public TransformerCoreBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    protected ScrollValueBehaviour turns;
    protected double power;
    protected double lastSentPower = -1;
    protected double heatDissipationFactor = 0;

    List<Float> primaryVoltages = new ArrayList<>();
    List<Float> secondaryVoltages = new ArrayList<>();
    float avgVoltage = 0;

    @OnlyIn(Dist.CLIENT)
    protected ElectricHumSoundInstance soundInstance;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        turns = new ScrollValueBehaviour(CEELang.translate("transformer.turns").component(), this, new ValueBox()) {
            @Override
            public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
                return new ValueSettingsBoard(label, max, 10, ImmutableList.of(CEELang.translate("transformer.turns_symbol").component()),
                        new ValueSettingsFormatter(ValueSettings::format));
            }
        };
        turns.between(1, 100);
        turns.value = 10;
        turns.withCallback(i -> this.updateTurns());
        behaviours.add(turns);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide)
            if (Math.abs(lastSentPower - power) > 100) {
                lastSentPower = power;
                sendData();
            }

        if (!level.isClientSide)
            return;

        CatnipServices.PLATFORM.executeOnClientOnly(() -> this::tickAudio);
    }

    @Override
    public void lazyTick() {
        Direction facing = getBlockState().getValue(TransformerCoreBlock.FACING);
        if (level.isClientSide || facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
            return;
        lastSentPower = power;

        InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
        InfrastructureSavedData.SimulatedDeviceInstance deviceInstance = sd.getDevice(worldPosition);
        if (deviceInstance == null) {
            sendData();
            return;
        }

        Set<BlockPos> visited = new HashSet<>();
        visited.add(worldPosition);
        heatDissipatorsDFS(visited, worldPosition);
        double dissipationFactor = 0;
        for (BlockPos pos : visited) {
            if (worldPosition.equals(pos))
                continue;
            dissipationFactor += 1d / Math.sqrt(pos.distSqr(worldPosition));
        }

        if (level.getFluidState(worldPosition).isEmpty())
            dissipationFactor *= 0.9;
        if (level.getFluidState(worldPosition.relative(getBlockState().getValue(TransformerCoreBlock.FACING))).isEmpty())
            dissipationFactor *= 0.9;
        dissipationFactor = dissipationFactor * 30000;

        deviceInstance.extraData().putDouble("HeatDissipationFactor", this.heatDissipationFactor = dissipationFactor);
        sendData();
    }

    private void heatDissipatorsDFS(Set<BlockPos> visited, BlockPos currentPos) {
        boolean waterlogged = !level.getFluidState(currentPos).isEmpty();
        for (Direction direction : Iterate.directions) {
            BlockPos nextPos = currentPos.relative(direction);
            if (nextPos.distSqr(worldPosition) > 4)
                continue;
            BlockState state = level.getBlockState(nextPos);
            if ((waterlogged && !level.getFluidState(currentPos).isEmpty()) ||
                    (CEEBlocks.TRANSFORMER_CORE.has(state) && direction == getBlockState().getValue(TransformerCoreBlock.FACING)) ||
                state.is(AllTags.optionalTag(BuiltInRegistries.BLOCK, CreateElecrtoEnergetics.rl("transformer_heat_dissipators"))))
                if (visited.add(nextPos))
                    heatDissipatorsDFS(visited, nextPos);
        }
    }




    @OnlyIn(Dist.CLIENT)
    protected void tickAudio() {
        Direction facing = getBlockState().getValue(TransformerCoreBlock.FACING);
        BlockPos otherPos = worldPosition.relative(facing);

        if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
            return;
        if (primaryVoltages.isEmpty())
            avgVoltage = 0;
        else
            avgVoltage = primaryVoltages.stream().reduce(Float::sum).orElse(0f) / primaryVoltages.size();

        Double vp1 = WireRenderer.getAllVoltages().get(new InWorldNode(0, worldPosition));
        Double vp2 = WireRenderer.getAllVoltages().get(new InWorldNode(1, worldPosition));
        Double vs1 = WireRenderer.getAllVoltages().get(new InWorldNode(0, otherPos));
        Double vs2 = WireRenderer.getAllVoltages().get(new InWorldNode(1, otherPos));
        if (vp1 != null && vp2 != null && vs1 != null && vs2 != null) {
            setPrimaryVoltage((float) Math.abs(vp1 - vp2));
            setSecondaryVoltage((float) Math.abs(vs1 - vs2));
            if (avgVoltage > 10) {
                if (soundInstance == null || soundInstance.isStopped())
                    Minecraft.getInstance()
                            .getSoundManager()
                            .play(soundInstance = new ElectricHumSoundInstance(worldPosition));
                else if (soundInstance != null) {
                    soundInstance.setVolume((float) Mth.clamp(power / 800000, 0.02, 0.125));
                    soundInstance.keepAlive();
                }
            } else if (soundInstance != null);
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Direction facing = getBlockState().getValue(TransformerCoreBlock.FACING);
        BlockPos otherPos = worldPosition.relative(facing);
        if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
            if (level.getBlockEntity(otherPos) instanceof TransformerCoreBlockEntity be)
                return be.addToGoggleTooltip(tooltip, isPlayerSneaking);
            else
                return false;
        }
        Double vp1 = WireRenderer.getAllVoltages().get(new InWorldNode(0, worldPosition));
        Double vp2 = WireRenderer.getAllVoltages().get(new InWorldNode(1, worldPosition));
        Double vs1 = WireRenderer.getAllVoltages().get(new InWorldNode(0, otherPos));
        Double vs2 = WireRenderer.getAllVoltages().get(new InWorldNode(1, otherPos));

        if (vp1 == null || vp2 == null || vs1 == null || vs2 == null)
            return false;
        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate("gui.goggles.electric_stats")
                .forGoggles(tooltip);
        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate("gui.goggles.primary_voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElecrtoEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(Math.abs(vp1 - vp2))))
                .translate("generic.volts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate("gui.goggles.secondary_voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElecrtoEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(Math.abs(vs1 - vs2))))
                .translate("generic.volts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate("gui.goggles.power")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElecrtoEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(power)))
                .translate("generic.watts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate("gui.goggles.max_heat_dissipation")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElecrtoEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(heatDissipationFactor)))
                .translate("generic.watts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);
        return true;
    }

    private void updateTurns() {
        if (!(level instanceof ServerLevel sl))
            return;
        Direction facing = getBlockState().getValue(TransformerCoreBlock.FACING);
        BlockPos otherPos = worldPosition.relative(facing);
        if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            if (level.getBlockEntity(otherPos) instanceof TransformerCoreBlockEntity be) {
                InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
                InfrastructureSavedData.SimulatedDeviceInstance deviceInstance = sd.getDevice(worldPosition);
                if (deviceInstance != null) {
                    deviceInstance.extraData().putDouble("Ratio", (double) turns.value / be.turns.value);
                }
            }
        } else {
            if (level.getBlockEntity(otherPos) instanceof TransformerCoreBlockEntity be) {
                be.updateTurns();
            }
        }


    }

    private void setPrimaryVoltage(float voltage) {
        if (primaryVoltages.size() >= 28)
            primaryVoltages.remove(0);
        primaryVoltages.add(voltage);
    }

    private void setSecondaryVoltage(float voltage) {
        if (secondaryVoltages.size() >= 28)
            secondaryVoltages.remove(0);
        secondaryVoltages.add(voltage);
    }


    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putDouble("HeatDissipationFactor", heatDissipationFactor);
        if (clientPacket)
            tag.putDouble("Power", power);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        heatDissipationFactor = tag.getDouble("HeatDissipationFactor");
        if (clientPacket)
            power = tag.getDouble("Power");
    }

    static class ValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 14);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return super.getLocalOffset(level, pos, state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction == state.getValue(TransformerBlock.FACING).getOpposite();
        }
    }
}
