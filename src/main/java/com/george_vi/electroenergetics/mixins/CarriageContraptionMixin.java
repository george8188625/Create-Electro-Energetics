package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.content.catenary.PantographBlock;
import com.george_vi.electroenergetics.content.catenary.PantographBlockEntity;
import com.george_vi.electroenergetics.mixin_interfaces.IPantographList;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import net.createmod.catnip.data.Pair;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
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
import java.util.List;

@Mixin(CarriageContraption.class)
public abstract class CarriageContraptionMixin extends Contraption implements IPantographList {
    @Unique
    public List<Pair<BlockPos, Boolean>> electroEnergetics$pantographs = new ArrayList<>();
    @Unique
    public boolean electroEnergetics$sidewaysPantograph = false;

    @Accessor("assemblyDirection")
    abstract Direction electroEnergetics$getAssemblyDirection();

    @Inject(method = "capture", at=@At("TAIL"), remap = false)
    public void electroEnergetics$assemble(Level level, BlockPos pos, CallbackInfoReturnable<oshi.util.tuples.Pair<StructureTemplate.StructureBlockInfo, BlockEntity>> cir) {
        BlockState state = level.getBlockState(pos);

        if (CEEBlocks.PANTOGRAPH.has(state) && level.getBlockEntity(pos) instanceof PantographBlockEntity be) {

            Direction facing = state.getValue(PantographBlock.FACING);
            if (facing.getAxis() != this.electroEnergetics$getAssemblyDirection().getAxis())
                electroEnergetics$sidewaysPantograph = true;
            else {
                electroEnergetics$pantographs.add(Pair.of(toLocalPos(pos), facing == this.electroEnergetics$getAssemblyDirection()));
            }
        }
    }

    @Override
    public void setPantographList(List<Pair<BlockPos, Boolean>> newPantographList) {
        electroEnergetics$pantographs = newPantographList;
    }

    @Override
    public List<Pair<BlockPos, Boolean>> getPantographList() {
        return electroEnergetics$pantographs;
    }
}
