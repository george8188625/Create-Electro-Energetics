package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.compat.computercraft.CCProxy;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.fuse.FuseHoldables;
import com.george_vi.electroenergetics.client.ElectricStatsTooltipModifier;
import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;

import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CreateElecrtoEnergetics.ID)
public class CreateElecrtoEnergetics
{
    public static final String ID = "electroenergetics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static CreateRegistrate REGISTRATE;

    public CreateElecrtoEnergetics(IEventBus modEventBus, ModContainer modContainer) {
        REGISTRATE = CreateRegistrate.create(ID)
                .setTooltipModifierFactory(item ->
                        new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                                .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
                                .andThen(TooltipModifier.mapNull(new ElectricStatsTooltipModifier(item)))
                );
        REGISTRATE.registerEventListeners(modEventBus);
        ModLoadingContext modLoadingContext = ModLoadingContext.get();

        CEEItems.register();
        CEEBlocks.register();
        CEEPackets.register();
        CEEPartialModels.register();
        CEEDisplaySources.register();
        CEEBlockEntityTypes.register();
        CEEWireTypes.register(modEventBus);
        CEEPantographTypes.register(modEventBus);
        CEESoundEvents.register(modEventBus);
        CEECreativeTab.register(modEventBus);
        CEEDataComponents.register(modEventBus);
        CEEWireAttachments.register(modEventBus);
        CEEElectricTrainSoundTypes.register(modEventBus);
        CEEWireInteractionBehaviours.register(modEventBus);
        CEEConfigs.register(modLoadingContext, modContainer);

        FuseHoldables.register();

        CCProxy.register();
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.tryBuild(ID, path);
    }
}
