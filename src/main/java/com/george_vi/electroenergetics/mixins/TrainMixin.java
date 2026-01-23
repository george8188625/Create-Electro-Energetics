package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEEElectricTrainSoundTypes;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.content.railway_electrification.ElectricTrainData;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.TrainSoundModifier;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundType;
import com.george_vi.electroenergetics.mixin_interfaces.ICEETrainExtension;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(Train.class)
public class TrainMixin implements ICEETrainExtension {
    @Unique
    ElectricTrainSoundType electroenergetics$soundType;

    @Unique
    int electroenergetics$accumulators = 0;

    @Unique
    double electroenergetics$accumulatorCharge = 0;

    @Unique
    ElectricTrainData electroenergetics$electricTrainData = new ElectricTrainData();

    @Unique
    public Set<TrainSoundModifier> electroEnergetics$soundModifyingBlocks = new HashSet<>();

    @Override
    public ElectricTrainSoundType getSoundType() {
        return electroenergetics$soundType;
    }

    @Override
    public void setSoundType(ElectricTrainSoundType type) {
        electroenergetics$soundType = type;
    }

    @Override
    public Set<TrainSoundModifier> getSoundModifyingBlocks() {
        return electroEnergetics$soundModifyingBlocks;
    }

    @Override
    public int getAccumulators() {
        return electroenergetics$accumulators;
    }

    @Override
    public void setAccumulators(int value) {
        electroenergetics$accumulators = value;
    }

    @Override
    public double getAccumulatorCharge() {
        return electroenergetics$accumulatorCharge;
    }

    @Override
    public void setAccumulatorCharge(double value) {
        electroenergetics$accumulatorCharge = value;
    }

    @Override
    public ElectricTrainData getElectricTrainData() {
        return electroenergetics$electricTrainData;
    }

    @Inject(method = "write", at=@At("RETURN"), remap = false)
    public void electroEnergetics$write(DimensionPalette dimensions, HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        ResourceLocation id = CEERegistries.ELECTRIC_TRAIN_SOUND_TYPE.getKey(electroenergetics$soundType);
        CompoundTag tag = cir.getReturnValue();
        if (id != null)
            tag.putString("CEETrainSoundType", id.toString());
        tag.putInt("CEEAccumulators", electroenergetics$accumulators);
        tag.putDouble("CEEAccumulatorCharge", electroenergetics$accumulatorCharge);
    }

    @Inject(method = "read", at=@At("RETURN"), remap = false)
    private static void electroEnergetics$read(CompoundTag tag, HolderLookup.Provider registries, Map<UUID, TrackGraph> trackNetworks, DimensionPalette dimensions, CallbackInfoReturnable<Train> cir) {
        String id = tag.getString("CEETrainSoundType");
        ResourceLocation location = ResourceLocation.tryParse(id);
        ElectricTrainSoundType soundType = null;
        if (location != null)
            soundType = CEERegistries.ELECTRIC_TRAIN_SOUND_TYPE.get(location);
        if (soundType == null)
            soundType = CEEElectricTrainSoundTypes.MODERN.get();

        ICEETrainExtension train = (ICEETrainExtension) cir.getReturnValue();
        train.setSoundType(soundType);
        train.setAccumulators(tag.getInt("CEEAccumulators"));
        train.setAccumulatorCharge(tag.getDouble("CEEAccumulatorCharge"));
    }

}
