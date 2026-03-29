package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CEEPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return CreateElectroEnergetics.ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {

        helper.forComponents(CEEBlocks.CONNECTOR.getId(), CEEBlocks.DOUBLE_CONNECTOR.getId(), CEEBlocks.TRIPLE_CONNECTOR.getId(), CEEBlocks.QUAD_CONNECTOR.getId(),
                        CEEItems.WIRE_SPOOL.getId(), CEEItems.COPPER_WIRE_SPOOL.getId(), CEEItems.IRON_WIRE_SPOOL.getId(), CEEItems.IRON_BUS_SPOOL.getId(),
                        CEEItems.CREATIVE_WIRE_SPOOL.getId(), CEEItems.HEAVILY_INSULATED_WIRE_SPOOL.getId())
                .addStoryBoard("basics", ElectricityBasicsScenes::theBasics)
                .addStoryBoard("connectors", ConnectorScenes::connectors)
                .addStoryBoard("connectors_chunks", ConnectorScenes::chunks);

        helper.forComponents(CEEBlocks.BULB.getId(), CEEBlocks.AMMETER.getId())
                .addStoryBoard("basics", ElectricityBasicsScenes::theBasics);

        helper.forComponents(CEEBlocks.TRANSFORMER.getId())
                .addStoryBoard("transformer_core", TransformerScenes::turns)
                .addStoryBoard("transformer", TransformerScenes::transformer)
                .addStoryBoard("transformer_losses", TransformerScenes::losses);

        helper.forComponents(CEEBlocks.TRANSFORMER_CORE.getId())
                .addStoryBoard("transformer_core", TransformerScenes::turns)
                .addStoryBoard("transformer_core_multiblock", TransformerScenes::transformerCore);

        helper.forComponents(CEEBlocks.PANTOGRAPH.getId(), CEEBlocks.CATENARY_HOLDER.getId())
                .addStoryBoard("railway_electrification", RailwayElectrificationScenes::setup)
                .addStoryBoard("electric_train_sounds", RailwayElectrificationScenes::sounds);

        helper.forComponents(CEEBlocks.RAIL_CONTACT_SHOE.getId())
                .addStoryBoard("third_rail", RailwayElectrificationScenes::thirdRail);

        helper.forComponents(CEEBlocks.DIODE.getId())
                .addStoryBoard("diode", ElectricityBasicsScenes::diode);

        helper.forComponents(CEEBlocks.VOLTMETER.getId())
                .addStoryBoard("voltage", ElectricityBasicsScenes::voltage);

        helper.forComponents(CEEBlocks.HV_SWITCH.getId())
                .addStoryBoard("hv_switch", SwitchScenes::hvSwitch);

        helper.forComponents(CEEBlocks.RELAY.getId())
                .addStoryBoard("relay", SwitchScenes::relay);

        helper.forComponents(CEEBlocks.CUT_OFF_SWITCH.getId(), CEEBlocks.DOUBLE_SWITCH.getId(), CEEBlocks.MOMENTARY_SWITCH.getId())
                .addStoryBoard("switch", SwitchScenes::cutOffSwitch);

        helper.forComponents(CEEBlocks.ALTERNATOR_ROTOR.getId())
                .addStoryBoard("alternator", GeneratorScenes::alternator);

        helper.forComponents(CEEBlocks.ALTERNATOR_BRUSHES.getId())
                .addStoryBoard("alternator", GeneratorScenes::alternator)
                .addStoryBoard("electric_train_sounds", RailwayElectrificationScenes::sounds);

        helper.forComponents(CEEBlocks.RESISTOR.getId())
                .addStoryBoard("resistance", ElectricityBasicsScenes::resistance);

        helper.forComponents(CEEBlocks.GROUND_ROD.getId())
                .addStoryBoard("grounding", ElectricityBasicsScenes::grounding);

        helper.forComponents(CEEBlocks.ENERGY_METER.getId())
                .addStoryBoard("energy_meter", InfrastructureScenes::energy_meter);
    }

    @Override
    public void registerSharedText(SharedTextRegistrationHelper helper) {
        helper.registerSharedText("voltage0", "0V");
        helper.registerSharedText("voltage12", "12V");
        helper.registerSharedText("voltage300", "300V");
        helper.registerSharedText("voltage600", "600V");
        helper.registerSharedText("voltage1500", "1500V");
        helper.registerSharedText("ratio1over2", "1 / 2");
        helper.registerSharedText("ratio1over3", "1 / 3");
        helper.registerSharedText("ratio3over1", "3 / 1");
        helper.registerSharedText("turns10", "10 turns");
        helper.registerSharedText("turns50", "50 turns");
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.registerTag(CreateElectroEnergetics.rl("electrical"))
                .addToIndex()
                .item(CEEBlocks.CONNECTOR, true, false)
                .title("Electrical Components")
                .description("Components that use electricity")
                .register();

        helper.registerTag(CreateElectroEnergetics.rl("electricity_basics"))
                .addToIndex()
                .item(CEEBlocks.DIODE, true, false)
                .title("Electricity Basics")
                .description("How electricity works")
                .register();

        helper.registerTag(CreateElectroEnergetics.rl("electric_train_sound_changing"))
                .addToIndex()
                .item(CEEBlocks.ALTERNATOR_BRUSHES, true, false)
                .title("Electric Train Sound Changers")
                .description("These blocks change electric train sounds.")
                .register();

        helper.addToTag(CreateElectroEnergetics.rl("electrical"))
                .add(CEEBlocks.CONNECTOR.getId())
                .add(CEEBlocks.DOUBLE_CONNECTOR.getId())
                .add(CEEBlocks.TRIPLE_CONNECTOR.getId())
                .add(CEEBlocks.QUAD_CONNECTOR.getId())
                .add(CEEItems.WIRE_SPOOL.getId())
                .add(CEEItems.HEAVILY_INSULATED_WIRE_SPOOL.getId())
                .add(CEEItems.COPPER_WIRE_SPOOL.getId())
                .add(CEEItems.IRON_WIRE_SPOOL.getId())
                .add(CEEItems.IRON_BUS_SPOOL.getId())
                .add(CEEItems.CREATIVE_WIRE_SPOOL.getId())
                .add(CEEBlocks.TRANSFORMER_CORE.getId())
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
                .add(CEEBlocks.GROUND_ROD.getId())
                ;

        helper.addToTag(CreateElectroEnergetics.rl("electricity_basics"))
                .add(CEEBlocks.BULB.getId())
                .add(CEEBlocks.VOLTMETER.getId())
                .add(CEEBlocks.AMMETER.getId())
                .add(CEEBlocks.DIODE.getId())
                .add(CEEBlocks.RESISTOR.getId())
                .add(CEEBlocks.CAPACITOR.getId())
                .add(CEEBlocks.RESISTOR.getId())
                .add(CEEBlocks.TRANSFORMER_CORE.getId())
                .add(CEEBlocks.GROUND_ROD.getId())
                ;

        helper.addToTag(CreateElectroEnergetics.rl("electric_train_sound_changing"))
                .add(CEEBlocks.ALTERNATOR_BRUSHES.getId())
                .add(CEEBlocks.MAGNET_BLOCK.getId())
                ;

        helper.addToTag(AllCreatePonderTags.TRAIN_RELATED)
                .add(CEEBlocks.PANTOGRAPH.getId())
                .add(CEEBlocks.CATENARY_HOLDER.getId())
                ;

        helper.addToTag(AllCreatePonderTags.DISPLAY_SOURCES)
                .add(CEEBlocks.AMMETER.getId())
                .add(CEEBlocks.VOLTMETER.getId())
                .add(CEEBlocks.ENERGY_METER.getId())
                ;
    }
}
