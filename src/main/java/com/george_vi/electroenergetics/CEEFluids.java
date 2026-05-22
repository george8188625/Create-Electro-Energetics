package com.george_vi.electroenergetics;

import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import org.jetbrains.annotations.NotNull;

import static com.george_vi.electroenergetics.CreateElectroEnergetics.REGISTRATE;

public class CEEFluids {

    public static final FluidEntry<BaseFlowingFluid.Flowing> TRANSFORMER_OIL = REGISTRATE.fluid("transformer_oil",
                    CreateElectroEnergetics.rl("block/fluid/transformer_oil_still"),
                    CreateElectroEnergetics.rl("block/fluid/transformer_oil_flow"))
            .properties(b -> b
                    .viscosity(2000)
                    .density(1400))
            .fluidProperties(p -> p
                    .levelDecreasePerBlock(2)
                    .tickRate(10)
                    .slopeFindDistance(3)
                    .explosionResistance(100f))
            .source(BaseFlowingFluid.Source::new)
            .block()
            .properties(p -> p.mapColor(MapColor.COLOR_GREEN))
            .build()
            .bucket()
            .onRegister(CEEFluids::registerFluidDispenseBehavior)
            .tag(Tags.Items.BUCKETS)
            .build()
            .register();

    public static final FluidEntry<BaseFlowingFluid.Flowing> PLANT_OIL = REGISTRATE.fluid("plant_oil",
                    CreateElectroEnergetics.rl("block/fluid/plant_oil_still"),
                    CreateElectroEnergetics.rl("block/fluid/plant_oil_flow"))
            .tag(CEETags.PLANT_OIL)
            .properties(b -> b
                    .viscosity(2000)
                    .density(1400))
            .fluidProperties(p -> p
                    .levelDecreasePerBlock(2)
                    .tickRate(10)
                    .slopeFindDistance(3)
                    .explosionResistance(100f))
            .source(BaseFlowingFluid.Source::new)
            .block()
            .properties(p -> p.mapColor(MapColor.COLOR_YELLOW))
            .build()
            .bucket()
            .onRegister(CEEFluids::registerFluidDispenseBehavior)
            .tag(Tags.Items.BUCKETS)
            .build()
            .register();

    private static final DispenseItemBehavior DEFAULT = new DefaultDispenseItemBehavior();
    private static final DispenseItemBehavior DISPENSE_FLUID = new DefaultDispenseItemBehavior() {
        @Override
        protected @NotNull ItemStack execute(BlockSource pSource, ItemStack pStack) {
            DispensibleContainerItem dispensibleContainerItem = (DispensibleContainerItem) pStack.getItem();
            BlockPos pos = pSource.pos().relative(pSource.state().getValue(DispenserBlock.FACING));
            Level level = pSource.level();
            if (dispensibleContainerItem.emptyContents(null, level, pos, null, pStack)) {
                return new ItemStack(Items.BUCKET);
            }
            return DEFAULT.dispense(pSource, pStack);
        }
    };

    private static void registerFluidDispenseBehavior(BucketItem bucket) {
        DispenserBlock.registerBehavior(bucket, DISPENSE_FLUID);
    }

    public static void register() {

    }
}
