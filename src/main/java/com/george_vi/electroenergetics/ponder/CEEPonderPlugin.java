package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CEEPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return CreateElecrtoEnergetics.ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {

        helper.forComponents(CEEBlocks.CONNECTOR.getId(), CEEBlocks.DOUBLE_CONNECTOR.getId(), CEEBlocks.TRIPLE_CONNECTOR.getId(), CEEBlocks.QUAD_CONNECTOR.getId(), CEEItems.WIRE_SPOOL.getId())
                .addStoryBoard("connectors", ConnectorScenes::connectors)
                .addStoryBoard("connectors_chunks", ConnectorScenes::chunks);
        helper.forComponents(CEEBlocks.TRANSFORMER.getId())
                .addStoryBoard("transformer", TransformerScenes::transformer)
                .addStoryBoard("transformer_losses", TransformerScenes::losses);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.registerTag(CreateElecrtoEnergetics.rl("electrical"))
                .addToIndex()
                .item(CEEBlocks.CONNECTOR, true, false)
                .title("Electrical Components")
                .description("Components that use electricity")
                .register();

        helper.addToTag(CreateElecrtoEnergetics.rl("electrical"))
                .add(CEEBlocks.CONNECTOR.getId())
                .add(CEEBlocks.DOUBLE_CONNECTOR.getId())
                .add(CEEBlocks.TRIPLE_CONNECTOR.getId())
                .add(CEEBlocks.QUAD_CONNECTOR.getId())
                .add(CEEBlocks.TRANSFORMER.getId())
                .add(CEEBlocks.ELECTRIC_MOTOR.getId())
                .add(CEEBlocks.ALTERNATOR_ROTOR.getId())
                .add(CEEBlocks.ALTERNATOR_BRUSHES.getId())
                .add(CEEBlocks.FUSE.getId())
                .add(CEEBlocks.VOLTMETER.getId())
                .add(CEEBlocks.AMMETER.getId())
                .add(CEEBlocks.ENERGY_METER.getId())
                .add(CEEBlocks.CUT_OFF_SWITCH.getId())
                .add(CEEBlocks.BULB.getId())
                ;
    }
}
