package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographBlock;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographBlockEntity;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.TrainPantographEntry;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ElectricTrainSounds;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.TrainSoundModifier;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundType;
import com.george_vi.electroenergetics.mixin_interfaces.IPantographList;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(CarriageContraption.class)
public abstract class CarriageContraptionMixin extends Contraption implements IPantographList {
    @Unique
    public List<TrainPantographEntry> electroEnergetics$pantographs = new ArrayList<>();
    @Unique
    public Set<TrainSoundModifier> electroEnergetics$soundModifyingBlocks = new HashSet<>();
    @Unique
    public boolean electroEnergetics$sidewaysPantograph = false;
    @Unique
    public boolean electroEnergetics$hasMotor = false;

    @Accessor("assemblyDirection")
    abstract Direction electroEnergetics$getAssemblyDirection();

    @Inject(method = "capture", at=@At("TAIL"), remap = false)
    public void electroEnergetics$capture(Level level, BlockPos pos, CallbackInfoReturnable<oshi.util.tuples.Pair<StructureTemplate.StructureBlockInfo, BlockEntity>> cir) {
        BlockState state = level.getBlockState(pos);

        if (CEEBlocks.PANTOGRAPH.has(state) && level.getBlockEntity(pos) instanceof PantographBlockEntity be) {

            Direction facing = state.getValue(PantographBlock.FACING);
            Direction assemblyDirection = this.electroEnergetics$getAssemblyDirection();
            if (facing.getAxis() != assemblyDirection.getAxis())
                electroEnergetics$sidewaysPantograph = true;
            else {
                electroEnergetics$pantographs.add(new TrainPantographEntry(toLocalPos(pos), toLocalPos(pos).rotate(
                        assemblyDirection == Direction.NORTH ? Rotation.COUNTERCLOCKWISE_90 :
                        assemblyDirection == Direction.EAST ? Rotation.CLOCKWISE_180 :
                        assemblyDirection == Direction.SOUTH ? Rotation.CLOCKWISE_90 : Rotation.NONE
                ), true, facing == assemblyDirection));
            }
        }
        if (state.is(AllTags.optionalTag(BuiltInRegistries.BLOCK, CreateElecrtoEnergetics.rl("train_electric_motor"))))
            electroEnergetics$hasMotor = true;
        electroEnergetics$addSMBlock(state.getBlock(), electroEnergetics$soundModifyingBlocks);
    }


    @Unique
    private static void electroEnergetics$addSMBlock(Block block, Set<TrainSoundModifier> smBlocks) {
        for (ElectricTrainSoundType soundType : CEERegistries.ELECTRIC_TRAIN_SOUND_TYPE) {
            if (soundType.validBlocks.get().containsKey(block)) {
                smBlocks.add(new TrainSoundModifier(block, soundType.validBlocks.get().getInt(block), soundType));
                return;
            }
        }
    }

    @Override
    public void setPantographList(List<TrainPantographEntry> newPantographList) {
        electroEnergetics$pantographs = newPantographList;
    }

    @Override
    public List<TrainPantographEntry> getPantographList() {
        return electroEnergetics$pantographs;
    }

    @Override
    public boolean hasElectricMotor() {
        return electroEnergetics$hasMotor;
    }

    @Override
    public void setElectricMotor(boolean v) {
        electroEnergetics$hasMotor = v;
    }

    @Override
    public Set<TrainSoundModifier> getSoundModifyingBlocks() {
        return electroEnergetics$soundModifyingBlocks;
    }
}
