package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CEEPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return CreateElecrtoEnergetics.ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {

        helper.forComponents(CEEBlocks.CONNECTOR.getId(), CEEBlocks.DOUBLE_CONNECTOR.getId(), CEEBlocks.TRIPLE_CONNECTOR.getId(), CEEBlocks.QUAD_CONNECTOR.getId(), CEEItems.WIRE_SPOOL.getId(), CEEItems.IRON_WIRE_SPOOL.getId(), CEEItems.IRON_BUS_SPOOL.getId(), CEEItems.CREATIVE_WIRE_SPOOL.getId())
                .addStoryBoard("connectors", ConnectorScenes::connectors)
                .addStoryBoard("connectors_chunks", ConnectorScenes::chunks);
        helper.forComponents(CEEBlocks.TRANSFORMER.getId())
                .addStoryBoard("transformer", TransformerScenes::transformer)
                .addStoryBoard("transformer_losses", TransformerScenes::losses);
        helper.forComponents(CEEBlocks.PANTOGRAPH.getId(), CEEBlocks.CATENARY_HOLDER.getId())
                .addStoryBoard("railway_electrification", RailwayElectrificationScenes::setup);
        helper.forComponents(CEEBlocks.DIODE.getId())
                .addStoryBoard("diode", ElectricityBasicsScenes::diode);
    }

    @Override
    public void registerSharedText(SharedTextRegistrationHelper helper) {
        helper.registerSharedText("voltage300", "300V");
        helper.registerSharedText("voltage600", "600V");
        helper.registerSharedText("ratio1over2", "1 / 2");
        helper.registerSharedText("ratio1over3", "1 / 3");
        helper.registerSharedText("ratio3over1", "3 / 1");
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.registerTag(CreateElecrtoEnergetics.rl("electrical"))
                .addToIndex()
                .item(CEEBlocks.CONNECTOR, true, false)
                .title("Electrical Components")
                .description("Components that use electricity")
                .register();

        helper.registerTag(CreateElecrtoEnergetics.rl("electricity_basics"))
                .addToIndex()
                .item(CEEBlocks.DIODE, true, false)
                .title("Electricity Basics")
                .description("How electricity works")
                .register();

        helper.addToTag(CreateElecrtoEnergetics.rl("electrical"))
                .add(CEEBlocks.CONNECTOR.getId())
                .add(CEEBlocks.DOUBLE_CONNECTOR.getId())
                .add(CEEBlocks.TRIPLE_CONNECTOR.getId())
                .add(CEEBlocks.QUAD_CONNECTOR.getId())
                .add(CEEItems.WIRE_SPOOL.getId())
                .add(CEEItems.IRON_WIRE_SPOOL.getId())
                .add(CEEItems.IRON_BUS_SPOOL.getId())
                .add(CEEItems.CREATIVE_WIRE_SPOOL.getId())
                .add(CEEBlocks.TRANSFORMER.getId())
                .add(CEEBlocks.VOLTAGE_REGULATOR.getId())
                .add(CEEBlocks.ELECTRIC_MOTOR.getId())
                .add(CEEBlocks.ALTERNATOR_ROTOR.getId())
                .add(CEEBlocks.ALTERNATOR_BRUSHES.getId())
                .add(CEEBlocks.FUSE.getId())
                .add(CEEBlocks.VOLTMETER.getId())
                .add(CEEBlocks.AMMETER.getId())
                .add(CEEBlocks.ENERGY_METER.getId())
                .add(CEEBlocks.TRI_POLAR_ENERGY_METER.getId())
                .add(CEEBlocks.BULB.getId())
                .add(CEEBlocks.CUT_OFF_SWITCH.getId())
                .add(CEEBlocks.HV_SWITCH.getId())
                .add(CEEBlocks.DOUBLE_SWITCH.getId())
                .add(CEEBlocks.PANTOGRAPH.getId())
                .add(CEEBlocks.CATENARY_HOLDER.getId())
                .add(CEEBlocks.DIODE.getId())
                .add(CEEBlocks.RESISTOR.getId())
                ;

        helper.addToTag(CreateElecrtoEnergetics.rl("electricity_basics"))
                .add(CEEBlocks.DIODE.getId())
                .add(CEEBlocks.RESISTOR.getId())
                ;

        helper.addToTag(AllCreatePonderTags.TRAIN_RELATED)
                .add(CEEBlocks.PANTOGRAPH.getId())
                .add(CEEBlocks.CATENARY_HOLDER.getId());
    }
}
